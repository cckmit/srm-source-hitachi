package org.srm.source.rfx.infra.repository.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.srm.boot.platform.configcenter.CnfHelper;
import org.srm.source.bid.infra.annotation.ProcessPriceShield;
import org.srm.source.rfx.api.dto.RfxQuotationHeaderDTO;
import org.srm.source.rfx.infra.mapper.HitachiRfxQuotationHeaderMapper;
import org.srm.source.rfx.infra.mapper.RfxQuotationHeaderMapper;
import org.srm.source.share.domain.entity.RoundHeader;
import org.srm.source.share.domain.entity.RoundHeaderDate;
import org.srm.source.share.domain.entity.SourceTemplate;
import org.srm.source.share.domain.entity.TmplFieldCol;
import org.srm.source.share.domain.repository.RoundHeaderDateRepository;
import org.srm.source.share.domain.repository.RoundHeaderRepository;
import org.srm.source.share.domain.repository.SourceTemplateRepository;
import org.srm.source.share.domain.repository.TmplFieldColRepository;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.constant.ShareConstants;
import org.srm.web.annotation.Tenant;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 报价单头表 资源库实现
 *
 * @author yuhao.guo@hand-china.com 2019-01-04 17:02:03
 */
@Component
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiRfxQuotationHeaderRepositoryImpl extends  RfxQuotationHeaderRepositoryImpl{

    @Autowired
    private RfxQuotationHeaderMapper rfxQuotationHeaderMapper;
    @Autowired
    private HitachiRfxQuotationHeaderMapper hitachiRfxQuotationHeaderMapper;
    @Autowired
    private RoundHeaderDateRepository roundHeaderDateRepository;
    @Autowired
    private SourceTemplateRepository sourceTemplateRepository;
    @Autowired
    private TmplFieldColRepository tmplFieldColRepository;
    @Autowired
    private RoundHeaderRepository roundHeaderRepository;

    @ProcessPriceShield
    @Override
    public RfxQuotationHeaderDTO selectQuotationHeaderByRfxQuotationHeaderId(Long tenantId, Long rfxQuotationHeaderId, Long supplierTenantId) {
        Date nowDate = new Date();
        RfxQuotationHeaderDTO rfxQuotationHeaderDTO = hitachiRfxQuotationHeaderMapper.selectQuotationHeaderByRfxQuotationHeaderId(tenantId, rfxQuotationHeaderId, supplierTenantId);
        if(Objects.isNull(rfxQuotationHeaderDTO)){
            return null;
        }
        Map<String, String> parameter = new HashMap<>(BaseConstants.Digital.ONE);
        parameter.put("company",rfxQuotationHeaderDTO.getCompanyId().toString());
        parameter.put("sourceCategory", rfxQuotationHeaderDTO.getSourceCategory());
        SourceTemplate sourceTemplate = sourceTemplateRepository.selectByPrimaryKey(rfxQuotationHeaderDTO.getTemplateId());
        parameter.put("sourceTemplate", sourceTemplate.getTemplateNum());
        String priceTypeCode = CnfHelper.select(rfxQuotationHeaderDTO.getTenantId(), ShareConstants.ConfigCenterCode.QUOTATION_SET, String.class).invokeWithParameter(parameter);
        rfxQuotationHeaderDTO.setPriceTypeCode(priceTypeCode);
        if (rfxQuotationHeaderDTO.getQuotationEndDate() == null) {
            return rfxQuotationHeaderDTO;
        }
        rfxQuotationHeaderDTO.initQuotationEndDate();
        Date quotationEndDate = rfxQuotationHeaderDTO.getQuotationEndDate();
        rfxQuotationHeaderDTO.setRemainingTime(quotationEndDate.getTime() - System.currentTimeMillis());
        rfxQuotationHeaderDTO.setCurrentDateTime(new Date());
        if (StringUtils.isNotBlank(rfxQuotationHeaderDTO.getRoundQuotationRule()) && rfxQuotationHeaderDTO.getRoundQuotationRule().contains(ShareConstants.RoundQuotationRule.AUTO)) {
            // 获取当前轮次截止时间
            RoundHeaderDate currentRoundHeaderDate = roundHeaderDateRepository.selectCurrentRoundDate(rfxQuotationHeaderDTO.getTenantId(), rfxQuotationHeaderDTO.getRfxHeaderId(), ShareConstants.SourceTemplate.CategoryType.RFX, rfxQuotationHeaderDTO.getRoundNumber(), new Date());
            if (currentRoundHeaderDate != null && currentRoundHeaderDate.getRoundQuotationEndDate() != null && currentRoundHeaderDate.getRoundQuotationEndDate().compareTo(nowDate) > 0) {
                rfxQuotationHeaderDTO.setQuotationRoundNumber(currentRoundHeaderDate.getQuotationRound());
                rfxQuotationHeaderDTO.setCurrentQuotationRound(currentRoundHeaderDate.getQuotationRound());
                rfxQuotationHeaderDTO.setQuotationEndDate(currentRoundHeaderDate.getRoundQuotationEndDate());
            }
        } else {
            if (!ShareConstants.RoundQuotationRule.NONE.equals(rfxQuotationHeaderDTO.getRoundQuotationRule())) {
                RoundHeader roundHeaderDb = roundHeaderRepository.selectOne(new RoundHeader(rfxQuotationHeaderDTO.getTenantId(), rfxQuotationHeaderDTO.getRfxHeaderId(), ShareConstants.SourceCategory.RFX));
                if (roundHeaderDb != null && roundHeaderDb.getRoundQuotationEndDate() != null && roundHeaderDb.getRoundQuotationEndDate().compareTo(nowDate) > 0) {
                    rfxQuotationHeaderDTO.setQuotationEndDate(roundHeaderDb.getRoundQuotationEndDate());
                    rfxQuotationHeaderDTO.setCurrentQuotationRound(roundHeaderDb.getQuotationRoundNumber());
                }
            }
        }
        // 关键字段显示控制
        setVisibleFlag(rfxQuotationHeaderDTO);
        return rfxQuotationHeaderDTO;
    }
    private void setVisibleFlag(RfxQuotationHeaderDTO rfxQuotationHeaderDTO) {
        List<TmplFieldCol> tmplFieldCols = tmplFieldColRepository.selectBySourceTemplateId(rfxQuotationHeaderDTO.getTemplateId(), rfxQuotationHeaderDTO.getTenantId());
        rfxQuotationHeaderDTO.setDefaultVisibleFlag();
        if(CollectionUtils.isEmpty(tmplFieldCols)){
            return;
        }
        //如果有资格预审，且供应商对应的资格预审行未审批通过
        if (!"NONE".equals(rfxQuotationHeaderDTO.getQualificationType())) {
            if (!Objects.equals("APPROVED", rfxQuotationHeaderDTO.getPreApproveStatus())) {
                //资格预审通过前
                tmplFieldCols.forEach(rfxQuotationHeaderDTO::setPrequalVisibleFlag);
            }
        }
    }
}
