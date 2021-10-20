package org.srm.source.share.app.service.impl;

import com.alibaba.fastjson.JSON;
import io.choerodon.core.exception.CommonException;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.redis.RedisHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.srm.boot.platform.configcenter.CnfHelper;
import org.srm.source.share.api.dto.QuotaTaxDTO;
import org.srm.source.share.api.dto.QuotaTaxReturnDTO;
import org.srm.source.share.app.service.HitachiQuotaTaxService;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.feign.HitachiSmdmRemoteService;
import org.srm.source.share.infra.feign.SmdmRemoteService;
import org.srm.web.annotation.Tenant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * 日本税率处理
 *
 * @author liujunjie
 */
@Service
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiQuotaTaxServiceImpl implements HitachiQuotaTaxService {

    private static final Logger LOGGER = LoggerFactory.getLogger(HitachiQuotaTaxServiceImpl.class);

    @Autowired
    private HitachiSmdmRemoteService hitachiSmdmRemoteService;

    @Autowired
    private RedisHelper redisHelper;

    @Override
    public <T> boolean handleQuotaTaxCalculation(Long tenantId, Map<String, String> cnfArgs, List<T> handleData, BiConsumer<QuotaTaxDTO, T> preHandler, BiConsumer<QuotaTaxDTO, T> postHandler) {
        // 根据配置 获取是否需要计算日本税率 (取缓存)
        Integer approveType = this.getJapanTaxFlagCache(tenantId, cnfArgs);
        if (!BaseConstants.Flag.YES.equals(approveType)) {
            return false;
        }
        handleQuotaTaxCalculation(tenantId, handleData, preHandler, postHandler);
        return true;
    }

    /**
     * 从缓存中取
     *
     * @param tenantId
     * @param cnfArgs
     * @return
     */
    private Integer getJapanTaxFlagCache(Long tenantId, Map<String, String> cnfArgs) {
        String redisKey = generateRedisKey(tenantId, cnfArgs);
        String approveTypeStr = redisHelper.strGet(generateRedisKey(tenantId, cnfArgs));
        if (StringUtils.isNotBlank(approveTypeStr)) {
            return Integer.parseInt(approveTypeStr);
        }
        Integer japanTaxFlag = CnfHelper.select(tenantId, HitachiConstants.ConfigCenterCode.JAPAN_TAX_CONFIG, Integer.class).invokeWithParameter(cnfArgs);
        if (Objects.isNull(japanTaxFlag)) {
            japanTaxFlag = BaseConstants.Flag.NO;
        }
        // 10s过期
        redisHelper.strSet(redisKey, japanTaxFlag.toString(), 10L, null);
        return japanTaxFlag;
    }


    @Override
    public <T> void handleQuotaTaxCalculation(Long tenantId, List<T> handleData, BiConsumer<QuotaTaxDTO, T> preHandler, BiConsumer<QuotaTaxDTO, T> postHandler) {
        // 对应关系taxDTO.id : T
        Map<Long, T> quotaTaxSourceMap = new HashMap<>(8);
        // 转换为税率实体
        List<QuotaTaxDTO> quotaTaxList = handleData.stream().map(e -> {
            QuotaTaxDTO quotaTaxDTO = new QuotaTaxDTO();
            preHandler.accept(quotaTaxDTO, e);
            quotaTaxSourceMap.put(quotaTaxDTO.getId(), e);
            return quotaTaxDTO;
        }).collect(Collectors.toList());
        ResponseEntity<QuotaTaxReturnDTO> result = hitachiSmdmRemoteService.getQuotaTax(tenantId, quotaTaxList);
        LOGGER.info("[MDM_QuotaTaxReturnDTO] Request:{} ; Response {} ", JSON.toJSON(quotaTaxList), JSON.toJSON(result));
        // 判断返回是否为空
        if (Objects.isNull(result) || (Objects.isNull(result.getBody()))) {
            throw new CommonException(BaseConstants.ErrorCode.NOT_FOUND);
        }
        // 判断返回是否处理失败
        QuotaTaxReturnDTO quotaTaxReturnDTO = result.getBody();
        if (BaseConstants.Flag.YES.equals(quotaTaxReturnDTO.getErrorFlag())) {
            throw new CommonException(quotaTaxReturnDTO.getErrorMessage());
        }
        List<QuotaTaxDTO> quotaTaxResultList = quotaTaxReturnDTO.getQuotaTaxList();
        quotaTaxResultList.forEach(quotaTaxResult -> {
            T sourceLine = quotaTaxSourceMap.get(quotaTaxResult.getId());
            postHandler.accept(quotaTaxResult, sourceLine);
        });
    }

    @Override
    public Integer getJapanTaxFlag(Long tenantId, Long companyId) {
        Map<String, String> cnfArgs = new HashMap<>(2);
        cnfArgs.put("companyId", String.valueOf(companyId));
        return CnfHelper.select(tenantId, HitachiConstants.ConfigCenterCode.JAPAN_TAX_CONFIG, Integer.class).invokeWithParameter(cnfArgs);
    }

    private String generateRedisKey(Long tenantId, Map<String, String> cnfArgs) {
        String cnfArgsStr = cnfArgs.entrySet().stream().map(e -> e.getKey().concat(e.getValue())).collect(Collectors.joining("_"));
        return String.format("ssrc:source:quota_tax:%s:%s", tenantId.toString(), cnfArgsStr);
    }


}
