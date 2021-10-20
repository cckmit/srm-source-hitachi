package org.srm.source.share.domain.service.impl;

import cfca.org.slf4j.Logger;
import cfca.org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.core.base.AopProxy;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.srm.boot.adaptor.client.AdaptorTaskHelper;
import org.srm.boot.adaptor.client.exception.TaskNotExistException;
import org.srm.boot.adaptor.client.result.TaskResultBox;
import org.srm.common.TenantInfoHelper;
import org.srm.common.client.entity.Tenant;
import org.srm.source.bid.domain.entity.BidMember;
import org.srm.source.bid.domain.repository.BidMemberRepository;
import org.srm.source.bid.infra.constant.BidConstants;
import org.srm.source.share.domain.entity.PriceAppScope;
import org.srm.source.share.domain.entity.PriceAppScopeLine;
import org.srm.source.share.api.dto.PriceLibServiceDTO;
// import org.srm.source.share.domain.entity.PriceAppScope;
// import org.srm.source.share.domain.entity.PriceAppScopeLine;
// import org.srm.source.share.domain.entity.PriceLibMain;
import org.srm.source.share.domain.entity.PurchaseOrganization;
import org.srm.source.share.domain.repository.HitachiPriceAppScopeLineRepository;
import org.srm.source.share.domain.repository.HitachiPriceAppScopeRepository;
import org.srm.source.share.domain.repository.PurchaseOrganizationRepository;
import org.srm.source.share.infra.utils.TaskAdaptorDataWrapper;
import org.srm.source.rfx.api.dto.HitachiOperationUnitDTO;
import org.srm.source.rfx.domain.entity.*;
import org.srm.source.rfx.domain.repository.RfxHeaderRepository;
import org.srm.source.rfx.domain.repository.RfxLadderQuotationRepository;
import org.srm.source.rfx.domain.repository.RfxMemberRepository;
import org.srm.source.rfx.domain.repository.RfxQuotationLineRepository;
import org.srm.source.rfx.infra.constant.SourceConstants;
import org.srm.source.rfx.infra.mapper.HitachiCommonQueryMapper;
import org.srm.source.share.domain.service.IPriceLibraryDomainService;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.constant.ShareConstants;

import java.util.*;
import java.util.stream.Collectors;

/**
 * description
 *
 * @author junjie.liu@hand-china.com
 */
@Service
@org.srm.web.annotation.Tenant(HitachiConstants.TENANT_NUM)
public class HitachiIPriceLibraryDomainServiceImpl extends IPriceLibraryDomainServiceImpl implements IPriceLibraryDomainService, AopProxy<IPriceLibraryDomainService> {

    @Autowired
    private RfxLadderQuotationRepository rfxLadderQuotationRepository;
    @Autowired
    private RfxMemberRepository rfxMemberRepository;
    @Autowired
    private BidMemberRepository bidMemberRepository;
    @Autowired
    private RfxHeaderRepository rfxHeaderRepository;
    @Autowired
    private RfxQuotationLineRepository rfxQuotationLineRepository;
    @Autowired
    private HitachiCommonQueryMapper hitachiCommonQueryMapper;
    @Autowired
    private HitachiPriceAppScopeRepository hitachiPriceAppScopeRepository;
    @Autowired
    private HitachiPriceAppScopeLineRepository hitachiPriceAppScopeLineRepository;
    @Autowired
    private PurchaseOrganizationRepository purchaseOrganizationRepository;

    private static final Logger logger = LoggerFactory.getLogger(IPriceLibraryDomainServiceImpl.class);

    @Override
    public List<PriceLibServiceDTO> buildPriceLibraryServiceParameters(Long tenantId, String priceLibTemplateCode, String sourceFrom, List<SourceResult> sourceResults) {
        // 构建价格服务头
        PriceLibServiceDTO priceLibServiceDTO = new PriceLibServiceDTO();
        priceLibServiceDTO.setSourceFrom(sourceFrom);
        priceLibServiceDTO.setTenantId(tenantId);
        priceLibServiceDTO.setTemplateCode(priceLibTemplateCode);
        if (CollectionUtils.isEmpty(sourceResults)) {
            return Collections.singletonList(priceLibServiceDTO);
        }
        Long sourceHeaderId = sourceResults.get(0).getSourceHeaderId();
        Long checkedUserId = DetailsHelper.getUserDetails().getUserId();
        if (ShareConstants.SourceTemplate.CategoryType.RFX.equals(sourceFrom)) {
            RfxMember rfxMember = rfxMemberRepository.selectOneRoleBySourceHeaderId(tenantId, sourceHeaderId, SourceConstants.RfxRole.CHECKED_BY);
            checkedUserId = rfxMember.getUserId();
        }
        if (ShareConstants.SourceTemplate.CategoryType.BID.equals(sourceFrom)) {
            BidMember bidMember = bidMemberRepository.selectOneBidMember(new BidMember(sourceHeaderId, tenantId, BidConstants.BidHeader.RoleType.SCALER));
            checkedUserId = bidMember.getUserId();
        }

        //新增适配器：传给接口侧前处理数据
        try {
            logger.info("=== SSRC_SOURCE_TO_PRICE_PRE_HANDLE AdaptorTask Before === {}",JSON.toJSON(sourceResults));
            Tenant tenant = TenantInfoHelper.selectByTenantId(tenantId);
            TaskAdaptorDataWrapper<List<SourceResult>, List<SourceResult>> dataWrapper = TaskAdaptorDataWrapper.wrapper(sourceResults);
            TaskResultBox taskResultBox = AdaptorTaskHelper.executeAdaptorTask("SSRC_SOURCE_TO_PRICE_PRE_HANDLE",tenant.getTenantNum(),dataWrapper);
            if (!org.springframework.util.CollectionUtils.isEmpty(taskResultBox.rowResult())) {
                // 取适配器优先级最高一行的执行结果为最后结果
                dataWrapper = TaskAdaptorDataWrapper.fromResult(taskResultBox,
                        new TypeReference<TaskAdaptorDataWrapper<List<SourceResult>, List<SourceResult>>>() {
                        });
            }
            sourceResults = dataWrapper.getOutData();
            logger.info("=== SSRC_SOURCE_TO_PRICE_PRE_HANDLE AdaptorTask Result === {}",JSON.toJSON(sourceResults));
        }catch (TaskNotExistException e){
            //无需处理
            logger.info("SourceResultNew AdaptorTask Not Found,tenantId:{}",tenantId);
        }catch (NullPointerException | IndexOutOfBoundsException | CommonException e) {
            // 适配器出参异常
            throw new CommonException(ShareConstants.ErrorCode.ADAPTOR_EXECUTE_ERROR, e);
        }

        // 构建价格服务行
        List<JSONObject> jsonObjects = new ArrayList<>(sourceResults.size());
        for (SourceResult sourceResult : sourceResults) {
            sourceResult.setSourceCode("SRM");
            sourceResult.setCreatedBy(checkedUserId);
            List<RfxLadderQuotation> rfxLadderQuotations = rfxLadderQuotationRepository.ladderQuotationByIds(sourceResult.getTenantId(), Collections.singletonList(sourceResult.getQuotationLineId()));
            if (CollectionUtils.isNotEmpty(rfxLadderQuotations)) {
                for (RfxLadderQuotation rfxLadderQuotation : rfxLadderQuotations) {
                    rfxLadderQuotation.setLadderPrice(rfxLadderQuotation.getValidLadderPrice());
                    rfxLadderQuotation.setLadderPriceRemark(rfxLadderQuotation.getRemark());
                    rfxLadderQuotation.setLadderLineNum(rfxLadderQuotation.getRfxLadderLineNum());
                    rfxLadderQuotation.setLadderNetPrice(rfxLadderQuotation.getValidNetLadderPrice());
                    rfxLadderQuotation.setCreatedBy(checkedUserId);
                }
                sourceResult.setPriceLibLadders(rfxLadderQuotations);
            }
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONStringWithDateFormat(sourceResult, BaseConstants.Pattern.DATETIME));
            RfxQuotationLine rfxQuotationLine = rfxQuotationLineRepository.selectByPrimaryKey(sourceResult.getQuotationLineId());
            // 获取是否一次性采购，一次性采购的不扩展价格范围
            if(BaseConstants.Flag.YES.equals(rfxQuotationLine.getAttributeTinyint10())){

            }else{
                // 构造价格范围
                List<PriceAppScope> priceAppScopeList = this.buildPiceAppScopeList(tenantId, sourceResult.getSourceHeaderId(), sourceResult.getQuotationLineId());
                jsonObject.put("priceAppScopes", priceAppScopeList);
                //价格适用范围字段
                jsonObject.put("attributeVarchar10",rfxQuotationLine.getAttributeVarchar10());
                jsonObjects.add(jsonObject);
            }
        }
        priceLibServiceDTO.setSaveVariables(jsonObjects);
        logger.info("=== Hitachi-priceAppScopes === {}",JSON.toJSON(jsonObjects));
        return Collections.singletonList(priceLibServiceDTO);
    }

    public List<PriceAppScope> buildPiceAppScopeList(Long tenantId, Long sourceHeaderId, Long quotationLineId){
        RfxHeader rfxHeader = rfxHeaderRepository.selectByPrimaryKey(sourceHeaderId);
        RfxQuotationLine rfxQuotationLine = rfxQuotationLineRepository.selectByPrimaryKey(quotationLineId);
        HitachiOperationUnitDTO queryParam = new HitachiOperationUnitDTO();
        queryParam.setTenantId(tenantId);
        queryParam.setSourceHeaderId(sourceHeaderId);
        queryParam.setQuotationLineId(quotationLineId);
        List<HitachiOperationUnitDTO> operationUnitDTOS = new ArrayList<>();
        List<PriceAppScope> priceAppScopeList = new ArrayList<>();

        //GG事务所购买 SY集約 ST集中attributeVarchar10
        if("GG".equals(rfxQuotationLine.getAttributeVarchar10())){
            operationUnitDTOS = hitachiCommonQueryMapper.selectOperationUnitsGG(queryParam);
            //事务所购买需要插入数据库一条采购组织信息
            List<Long> purOrganizationIds = new ArrayList<>();
            //List<String> purOrganizationIdsStr = Arrays.asList(rfxQuotationLine.getAttributeVarchar23().split(","));
            //purOrganizationIdsStr.forEach(purOrganizationIdStr -> purOrganizationIds.add(Long.parseLong(purOrganizationIdStr)));
            PriceAppScope priceAppScope = new PriceAppScope();
            priceAppScope.setTenantId(tenantId);
            priceAppScope.setDimensionCode("purOrganizationId");
            List<PriceAppScopeLine> priceAppScopeLinesPo = hitachiCommonQueryMapper.selectPurOrganizationsByIds(tenantId, purOrganizationIds);
            priceAppScope.setPriceAppScopeLines(priceAppScopeLinesPo);
            //知道了purOrganizationId，等于知道了全部 表hpfm_purchase_organization
            Long purchaseOrgId = rfxHeader.getPurOrganizationId();
            PurchaseOrganization purchaseOrganization = purchaseOrganizationRepository.selectCodeAndName(purchaseOrgId);
            String dataCode=purchaseOrganization.getOrganizationCode();
            String dataName=purchaseOrganization.getOrganizationName();
            Long dataId=purchaseOrgId;
            //插入即可
            //this.hitachiPriceAppScopeLineRepository.batchInsertSelective((List<PriceAppScopeLine>) new PriceAppScopeLine(tenantId, priceAppScope.getAppScopeId(), dataId, dataCode, dataName));
            //this.hitachiPriceAppScopeLineRepository.insertSelective(new PriceAppScopeLine(tenantId, priceAppScope.getAppScopeId(), dataId, dataCode, dataName));
        }else if("SY".equals(rfxQuotationLine.getAttributeVarchar10()) || "ST".equals(rfxQuotationLine.getAttributeVarchar10())){
            //价格适用范围是集中的时候 如果采购组织没有输入 就插入全部的采购组织
            if ("ST".equals(rfxQuotationLine.getAttributeVarchar10()) && rfxHeader.getPurOrganizationId()==null ){
                //todo
            }
            // operationUnitDTOS = hitachiCommonQueryMapper.selectOperationUnitsSY(queryParam);

            /**
             * 两个map,一个com查ou,一个ou查inv
             * 向下1，根据comA查ouA,判断字段ouB是否包含在ouA中，在 则不动，不在将A+B赋值C
             * 向下2，根据ouB查invA,判断字段invB是否包含在invA中，在 则不动，不在将A+B赋值C
             * 向上，直接用 最终invC,查出ouC,comC 成
            **/

            // attributeVarchar20公司 attributeVarchar21业务实体 attributeVarchar22库存组织 attributeVarchar23采购组织
            List<Long> companyIds = new ArrayList<>();
            List<Long> ouIds = new ArrayList<>();
            List<Long> invOrganizationIds = new ArrayList<>();

            if(StringUtils.isNotBlank(rfxQuotationLine.getAttributeVarchar20())){
                List<String> companyIdsStr = Arrays.asList(rfxQuotationLine.getAttributeVarchar20().split(","));
                companyIdsStr.forEach(companyIdStr -> companyIds.add(Long.parseLong(companyIdStr)));
            }
            if(StringUtils.isNotBlank(rfxQuotationLine.getAttributeVarchar21())) {
                List<String> ouIdsStr = Arrays.asList(rfxQuotationLine.getAttributeVarchar21().split(","));
                ouIdsStr.forEach(ouIdStr -> ouIds.add(Long.parseLong(ouIdStr)));
            }
            if(StringUtils.isNotBlank(rfxQuotationLine.getAttributeVarchar22())) {
                List<String> invOrganizationIdsStr = Arrays.asList(rfxQuotationLine.getAttributeVarchar22().split(","));
                invOrganizationIdsStr.forEach(invOrganizationIdStr -> invOrganizationIds.add(Long.parseLong(invOrganizationIdStr)));
            }
            //向下扩展
            List<HitachiOperationUnitDTO> allComOuInv =  hitachiCommonQueryMapper.selectAllComOuInv(tenantId, null);
            Map<Long, List<HitachiOperationUnitDTO>> comMap = allComOuInv.stream().collect(Collectors.groupingBy(HitachiOperationUnitDTO::getCompanyId));
            Map<Long, List<HitachiOperationUnitDTO>> ouMap = allComOuInv.stream().collect(Collectors.groupingBy(HitachiOperationUnitDTO::getOuId));
            companyIds.forEach(companyId ->{
                List<HitachiOperationUnitDTO> hitachiOperationUnitDTOList = comMap.get(companyId);
                List<Long> extendOuIds = new ArrayList<>();
                if(CollectionUtils.isNotEmpty(hitachiOperationUnitDTOList)){
                    extendOuIds = comMap.get(companyId).stream().map(HitachiOperationUnitDTO::getOuId).collect(Collectors.toList());
                }
                                // 有交集不操作,无交集add
                if(Collections.disjoint(extendOuIds, ouIds) && CollectionUtils.isNotEmpty(extendOuIds)){
                    ouIds.addAll(extendOuIds);
                }
            });
            List<Long> tempOuIds = ouIds.stream().distinct().collect(Collectors.toList());
            tempOuIds.forEach(ouId ->{
                List<Long> extendInvOrganizationIds = new ArrayList<>();
                List<HitachiOperationUnitDTO> hitachiOperationUnitDTOList = ouMap.get(ouId);
                if(CollectionUtils.isNotEmpty(hitachiOperationUnitDTOList)){
                    extendInvOrganizationIds = ouMap.get(ouId).stream().map(HitachiOperationUnitDTO::getOrganizationId).collect(Collectors.toList());
                }
                // 有交集不操作,无交集add
                if(Collections.disjoint(extendInvOrganizationIds, invOrganizationIds) && CollectionUtils.isNotEmpty(extendInvOrganizationIds)){
                    invOrganizationIds.addAll(extendInvOrganizationIds);
                }
            });
            List<Long> endInvOrganizationIds = invOrganizationIds.stream().distinct().collect(Collectors.toList());
            //向上补充
            List<HitachiOperationUnitDTO> endAllComOuInv =  hitachiCommonQueryMapper.selectAllComOuInv(tenantId, endInvOrganizationIds);
            List<Long> endOuIds = endAllComOuInv.stream().map(HitachiOperationUnitDTO::getOuId).distinct().collect(Collectors.toList());
            List<Long> endCompanyIds = endAllComOuInv.stream().map(HitachiOperationUnitDTO::getCompanyId).distinct().collect(Collectors.toList());

            if(CollectionUtils.isNotEmpty(endCompanyIds)){
                PriceAppScope priceAppScopeCom = new PriceAppScope();
                priceAppScopeCom.setTenantId(tenantId);
                priceAppScopeCom.setDimensionCode("companyId");
                List<PriceAppScopeLine> priceAppScopeLinesCom = hitachiCommonQueryMapper.selectCompanysByIds(tenantId, endCompanyIds);
                priceAppScopeCom.setPriceAppScopeLines(priceAppScopeLinesCom);
                priceAppScopeList.add(priceAppScopeCom);
            }

            if(CollectionUtils.isNotEmpty(endOuIds)){
                PriceAppScope priceAppScopeOu = new PriceAppScope();
                priceAppScopeOu.setTenantId(tenantId);
                priceAppScopeOu.setDimensionCode("ouId");
                List<PriceAppScopeLine> priceAppScopeLinesOu = hitachiCommonQueryMapper.selectOperationUnitsByIds(tenantId, endOuIds);
                priceAppScopeOu.setPriceAppScopeLines(priceAppScopeLinesOu);
                priceAppScopeList.add(priceAppScopeOu);
            }

            if(CollectionUtils.isNotEmpty(endInvOrganizationIds)){
                PriceAppScope priceAppScopeIo = new PriceAppScope();
                priceAppScopeIo.setTenantId(tenantId);
                priceAppScopeIo.setDimensionCode("invOrganizationId");
                List<PriceAppScopeLine> priceAppScopeLinesIo = hitachiCommonQueryMapper.selectInvOrganizationsByIds(tenantId, endInvOrganizationIds);
                priceAppScopeIo.setPriceAppScopeLines(priceAppScopeLinesIo);
                priceAppScopeList.add(priceAppScopeIo);
            }

            if(StringUtils.isNotBlank(rfxQuotationLine.getAttributeVarchar23())) {
                List<String> purOrganizationIdsStr = Arrays.asList(rfxQuotationLine.getAttributeVarchar23().split(","));
                List<Long> purOrganizationIds = new ArrayList<>();
                purOrganizationIdsStr.forEach(purOrganizationIdStr -> purOrganizationIds.add(Long.parseLong(purOrganizationIdStr)));

                if(CollectionUtils.isNotEmpty(purOrganizationIds)){
                    PriceAppScope priceAppScopePo = new PriceAppScope();
                    priceAppScopePo.setTenantId(tenantId);
                    priceAppScopePo.setDimensionCode("purOrganizationId");
                    List<PriceAppScopeLine> priceAppScopeLinesPo = hitachiCommonQueryMapper.selectPurOrganizationsByIds(tenantId, purOrganizationIds);
                    priceAppScopePo.setPriceAppScopeLines(priceAppScopeLinesPo);
                    priceAppScopeList.add(priceAppScopePo);
                }
            }


        }
        // else if("ST".equals(rfxQuotationLine.getAttributeVarchar10())){
        //     // operationUnitDTOS = hitachiCommonQueryMapper.selectOperationUnitsST(queryParam);;
        // }

        // List<PriceAppScope> priceAppScopeList = new ArrayList<>();
        /*Map<Long, List<HitachiOperationUnitDTO>> collect = operationUnitDTOS.stream().collect(Collectors.groupingBy(HitachiOperationUnitDTO::getCompanyId));

        for (Map.Entry<Long, List<HitachiOperationUnitDTO>> longListEntry : collect.entrySet()) {
            PriceAppScope priceAppScope = new PriceAppScope();
        }*/

        List<HitachiOperationUnitDTO> operationUnitDTOSCom = operationUnitDTOS.stream().collect(Collectors.collectingAndThen(
                Collectors.toCollection(() -> new TreeSet<>(
                        Comparator.comparing(HitachiOperationUnitDTO::getCompanyId))), ArrayList::new));
        // companyId
        /*PriceAppScope priceAppScopeCom = new PriceAppScope();
        priceAppScopeCom.setTenantId(tenantId);
        priceAppScopeCom.setDimensionCode("companyId");
        List<PriceAppScopeLine> priceAppScopeLinesCom = new ArrayList<>();
        for(HitachiOperationUnitDTO operationUnitDTO : operationUnitDTOSCom) {
            PriceAppScopeLine priceAppScopeLine = new PriceAppScopeLine();
            priceAppScopeLine.setTenantId(tenantId);
            priceAppScopeLine.setDataId(operationUnitDTO.getCompanyId());
            priceAppScopeLine.setDataCode(operationUnitDTO.getCompanyCode());
            priceAppScopeLine.setDataName(operationUnitDTO.getCompanyName());
            priceAppScopeLinesCom.add(priceAppScopeLine);
        };
        priceAppScopeCom.setPriceAppScopeLines(priceAppScopeLinesCom);
        priceAppScopeList.add(priceAppScopeCom);
        // ouId
        PriceAppScope priceAppScopeOu = new PriceAppScope();
        priceAppScopeOu.setTenantId(tenantId);
        priceAppScopeOu.setDimensionCode("ouId");
        List<PriceAppScopeLine> priceAppScopeLinesOu = new ArrayList<>();
        for(HitachiOperationUnitDTO operationUnitDTO : operationUnitDTOS) {
            PriceAppScopeLine priceAppScopeLine = new PriceAppScopeLine();
            priceAppScopeLine.setTenantId(tenantId);
            priceAppScopeLine.setDataId(operationUnitDTO.getOuId());
            priceAppScopeLine.setDataCode(operationUnitDTO.getOuCode());
            priceAppScopeLine.setDataName(operationUnitDTO.getOuName());
            priceAppScopeLinesOu.add(priceAppScopeLine);
        };
        priceAppScopeOu.setPriceAppScopeLines(priceAppScopeLinesOu);
        priceAppScopeList.add(priceAppScopeOu);*/

        return priceAppScopeList;
    }

}
