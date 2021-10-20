package org.srm.source.rfx.domain.service.impl;

import com.alibaba.fastjson.JSON;
import io.choerodon.core.exception.CommonException;
import io.choerodon.core.oauth.DetailsHelper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.file.FileClient;
import org.hzero.boot.file.dto.FileDTO;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.Regexs;
import org.hzero.core.util.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;
import org.srm.boot.adaptor.client.AdaptorMappingHelper;
import org.srm.boot.adaptor.client.AdaptorTaskHelper;
import org.srm.boot.adaptor.client.exception.TaskNotExistException;
import org.srm.boot.adaptor.client.result.TaskResultBox;
import org.srm.boot.platform.configcenter.CnfHelper;
import org.srm.common.TenantInfoHelper;
import org.srm.source.rfx.api.dto.RfxLineSupplierDTO;
import org.srm.source.rfx.app.service.RfxHeaderService;
import org.srm.source.rfx.app.service.RfxLineSupplierService;
import org.srm.source.rfx.app.service.impl.RfxHeaderServiceImpl;
import org.srm.source.rfx.domain.entity.RfxHeader;
import org.srm.source.rfx.domain.entity.RfxLineItem;
import org.srm.source.rfx.domain.entity.RfxLineSupplier;
import org.srm.source.rfx.domain.repository.RfxItemSupAssignRepository;
import org.srm.source.rfx.domain.repository.RfxLineItemRepository;
import org.srm.source.rfx.domain.repository.RfxLineSupplierRepository;
import org.srm.source.rfx.domain.service.IRfxLineItemDomainService;
import org.srm.source.rfx.domain.service.IRfxLineSupplierDomainService;
import org.srm.source.rfx.infra.constant.SourceConstants;
import org.srm.source.rfx.infra.util.CopyUtils;
import org.srm.source.share.api.dto.PrHeader;
import org.srm.source.share.api.dto.PrLine;
import org.srm.source.share.api.dto.PrLineDTO;
import org.srm.source.share.api.dto.SourceLineSupplierDTO;
import org.srm.source.share.domain.entity.SourceTemplate;
import org.srm.source.share.domain.vo.PrLineVO;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.constant.ShareConstants;
import org.srm.source.share.infra.convertor.CommonConvertor;
import org.srm.source.share.infra.feign.SpucRemoteService;
import org.srm.web.annotation.Tenant;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * @author feng
 * @date 2021.09.06
 */
@Component
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiIRfxHeaderDomainServiceImpl extends IRfxHeaderDomainServiceImpl {

    @Autowired
    @Lazy
    private RfxLineSupplierRepository rfxLineSupplierRepository;
    @Autowired
    private RfxItemSupAssignRepository itemSupAssignRepository;
    @Autowired
    @Lazy
    private IRfxLineSupplierDomainService rfxLineSupplierDomainService;
    @Autowired
    private RfxLineItemRepository rfxLineItemRepository;
    @Autowired
    @Lazy
    private RfxHeaderService rfxHeaderService;
    @Autowired
    @Lazy
    private RfxLineSupplierService rfxLineSupplierService;
    @Autowired
    @Lazy
    private IRfxLineItemDomainService rfxLineItemDomainService;
    @Autowired
    @Lazy
    private SpucRemoteService spucRemoteService;
    @Autowired
    private FileClient fileClient;

    private static  final String PRIVATE_BUCKET = "private-bucket";
    private static  final String DIRECTORY_SODR_ORDER = "ssrc-rfx-rfxitem";

    private static final Logger LOGGER = LoggerFactory.getLogger(HitachiIRfxHeaderDomainServiceImpl.class);

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<RfxLineSupplier> processRfxLineSupplier(RfxHeader header, List<RfxLineSupplier> supplierList) {
        if (CollectionUtils.isEmpty(supplierList)) {
            return supplierList;
        }
        if (header.getRoundNumber() == 1 && !SourceConstants.RfxType.INVITE.equals(header.getSourceMethod())) {
            RfxLineSupplier rfxLineSupplierParam = new RfxLineSupplier();
            rfxLineSupplierParam.setRfxHeaderId(header.getRfxHeaderId());
            rfxLineSupplierRepository.delete(rfxLineSupplierParam);
            itemSupAssignRepository.deleteItemSupAssignByHeaderId(header.getRfxHeaderId());
            return new ArrayList<>();
        }
        if (!ShareConstants.SourceTemplate.RankRule.WEIGHT_PRICE.equals(header.getRankRule())) {
            supplierList.forEach(rfxLineSupplier -> rfxLineSupplier.setPriceCoefficient(null));
        }

        List<RfxLineSupplier> suppliers = rfxLineSupplierDomainService.createOrUpdateLineSupplier(header.getTenantId(), header.getRfxHeaderId(), supplierList);
        rfxLineSupplierDomainService.initSupplierItemAssign(suppliers);
        return supplierList;
    }

    @Override
    public RfxHeader createRfxHeaderAndLineItem(RfxHeader rHeader, Long organizationId, List<PrLineVO> prLineList) {
        //Map<Long, Long> mapId = new HashMap<>();
        LongAdder longAdder = new LongAdder();
        longAdder.add(0L);
        SourceTemplate sourceTemplate = rHeader.getSourceTemplate();
        List<RfxLineItem> rfxLineItems = new ArrayList<>();
        Set<Long> prLineIds=new HashSet<>();
        //获取租户信息
        org.srm.common.client.entity.Tenant tenant = TenantInfoHelper.selectByTenantId(organizationId);
        prLineList = prLineList.stream().sorted(Comparator.comparing(PrLineVO::getDisplayPrNum).thenComparing(PrLineVO::getLineNum)).collect(Collectors.toList());
        for (int i = 0; i < prLineList.size(); i++) {
            longAdder.increment();
            PrLineVO prLineVO = prLineList.get(i);
            prLineIds.add(prLineVO.getPrLineId());
            //创建物料行
            RfxLineItem rfxLineItem = CommonConvertor.beanConvert(RfxLineItem.class, prLineVO);
            rfxLineItem.setRfxHeaderId(rHeader.getRfxHeaderId());
            rfxLineItem.setTenantId(organizationId);
            rfxLineItem.setItemCode(prLineVO.getItemCode());
            //3.24改为剩余数量
            rfxLineItem.setRfxQuantity(prLineVO.getOccupiedQuantity());
            rfxLineItem.setRfxLineItemNum(longAdder.longValue());
            rfxLineItem.setItemCategoryId(prLineVO.getCategoryId());
            rfxLineItem.setDemandDate(prLineVO.getNeededDate());
            rfxLineItem.setPrLineNum(prLineVO.getLineNum());
            rfxLineItem.setPrDisplayLineNum(prLineVO.getDisplayLineNum());
            rfxLineItem.setPrNum(prLineVO.getDisplayPrNum());
            rfxLineItem.setPrHeaderId(prLineVO.getPrHeaderId());
            rfxLineItem.setPrLineId(prLineVO.getPrLineId());
            //如果申请税率为空，用模板作为默认值
            rfxLineItem.setTaxIncludedFlag(Objects.nonNull(prLineVO.getTaxId()) ? BaseConstants.Flag.YES : sourceTemplate.getTaxIncludedFlag());
            rfxLineItem.setTaxId(Objects.nonNull(prLineVO.getTaxId())?prLineVO.getTaxId():sourceTemplate.getTaxId());
            rfxLineItem.setTaxRate(Objects.nonNull(prLineVO.getTaxRate())?prLineVO.getTaxRate():sourceTemplate.getTaxRate());
            rfxLineItem.setFreightIncludedFlag(sourceTemplate.getFreightIncludedFlag());
            String uuid = UUIDUtils.generateTenantUUID(organizationId);
            List<FileDTO> fileDTOList = fileClient.getAttachmentFiles(organizationId,PRIVATE_BUCKET,prLineVO.getAttachmentUuid());
            for (FileDTO fileDTO:fileDTOList) {
                InputStream inputStream = fileClient.downloadFile(organizationId,PRIVATE_BUCKET,fileDTO.getFileUrl());
                try {
                    fileClient.uploadAttachment(organizationId,PRIVATE_BUCKET,DIRECTORY_SODR_ORDER,
                            uuid,fileDTO.getFileName(), FileCopyUtils.copyToByteArray(inputStream));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fileClient.getAttachmentFileCount(organizationId,PRIVATE_BUCKET,uuid) != 0){
                rfxLineItem.setAttachmentUuid(uuid);
            }
            rfxLineItem.setSupplierItemNum(prLineVO.getSupplierItemCode());
            rfxLineItem.setDrawingNum(prLineVO.getDrawingNum());
            rfxLineItem.setSurfaceFlag(prLineVO.getSurfaceTreatFlag());
            rfxLineItem.setDrawingVersionNumber(prLineVO.getDrawingVersion());
            rfxLineItem.setExecutedBy(DetailsHelper.getUserDetails().getUserId());
            rfxLineItem.setExecutedDate(new Date());
            rfxLineItem.setExecutionStatusCode(SourceConstants.RfxStatus.FINISHED);
            rfxLineItem.setExecutionBillId(rHeader.getRfxHeaderId());
            rfxLineItem.setExecutionBillNum(ShareConstants.PriceLibrary.PriceSourceStatus.SOURCE);
            rfxLineItem.setExecutionBillData(rHeader.getRfxNum());
            rfxLineItem.setSourceFrom(ShareConstants.SourceCategory.RFX);
            rfxLineItem.setSupplierItemNumDesc(rfxLineItem.getSupplierItemNumDesc());
            //属性字段
            rfxLineItem.setItemProperties(prLineVO.getItemProperties());
            rfxLineItem.setSpecs(prLineVO.getItemSpecs());
            rfxLineItem.setModel(prLineVO.getItemModel());
            //采购申请行含税金额,含税单价
            rfxLineItem.setPrTaxLineAmount(prLineVO.getTaxIncludedLineAmount());
            rfxLineItem.setPrTaxUnitPrice(prLineVO.getTaxIncludedUnitPrice());
            rfxLineItem.setPrTaxBudgetUnitPrice(prLineVO.getTaxIncludedBudgetUnitPrice());
            //采购申请来源平台
            rfxLineItem.setPrSourcePlatform(prLineVO.getPrSourcePlatform());
            //项目号和项目名称
            rfxLineItem.setItemProjectNum(prLineVO.getProjectNum());
            rfxLineItem.setItemProjectName(prLineVO.getProjectName());
            //行备注
            rfxLineItem.setLineItemRemark(prLineVO.getRemark());
            //采购申请行预估金额
            rfxLineItem.setEstimatedPrice(prLineVO.getTaxIncludedUnitPrice());
            rfxLineItem.setEstimatedAmount(prLineVO.getTaxIncludedLineAmount());
            rfxLineItem.setNetEstimatedPrice(prLineVO.getUnitPrice());
            rfxLineItem.setNetEstimatedAmount(prLineVO.getLineAmount());

            //查出规格字段
            if(Objects.nonNull(prLineVO.getItemId())){
                RfxLineItem param = new RfxLineItem();
                param.setItemId(prLineVO.getItemId());
                param = this.rfxLineItemRepository.getItemInfoByItemId(param);
                if(Objects.nonNull(param)){
                    rfxLineItem.setSpecs(Optional.ofNullable(rfxLineItem.getSpecs()).orElse(param.getSpecs()));
                    rfxLineItem.setModel(Optional.ofNullable(rfxLineItem.getModel()).orElse(param.getModel()));
                }
            }
            //根据配置器获取数据
            try {
                TaskResultBox taskResultBox = AdaptorTaskHelper.executeAdaptorTask(ShareConstants.AdaptorTask.SSRC_PR_TO_SOURCE_ITEM_LINE, tenant.getTenantNum(), prLineVO);
                RfxLineItem rfxLineItemCopy = taskResultBox.get(0, RfxLineItem.class);
                BeanUtils.copyProperties(rfxLineItemCopy,rfxLineItem, CopyUtils.getNullPropertyNames(rfxLineItemCopy));
            }catch (TaskNotExistException e){
                LOGGER.info("============SSRC_PR_TO_SOURCE_ITEM_LINE-TaskNotExistException=============={}",tenant.getTenantNum(),e.getMessage(),e.getStackTrace());
            }
            if(rfxLineItem.getBatchPrice() == null){
                rfxLineItem.setBatchPrice(BigDecimal.ONE);//临时处理方案
            }
            rfxLineItems.add(rfxLineItem);
//            mapId.put(prLineVO.getPrLineId(),rfxLineItem.getRfxLineItemId());
        }

        // 字段映射
        List<RfxHeader> results = fieldMap(organizationId, rHeader, rfxLineItems);
        List<RfxLineItem> resultLines = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(results)) {
            rHeader =  results.get(0);
            results.forEach(r -> resultLines.add(r.getLine()));
        }
        if (CollectionUtils.isNotEmpty(resultLines)) {
            rfxLineItems = resultLines;
        }
        // 保存询价单头
        rfxHeaderService.saveOrUpdateHeader(rHeader);
        Long rfxHeaderId = rHeader.getRfxHeaderId();
        rfxLineItems.forEach(line -> line.setRfxHeaderId(rfxHeaderId));

        this.rfxLineItemRepository.batchInsertSelective(rfxLineItems);
        if(ShareConstants.SourceTemplate.SourceMethod.INVITE.equals(rHeader.getSourceMethod())){
            //供应商-物料维度
            Map<Long,List<Long>> supplierItems=new HashMap<>();
            List<RfxLineSupplierDTO> supplierDTOS=rfxLineItemRepository.selectSupplierByPrLineIds(prLineIds);
            if(CollectionUtils.isNotEmpty(supplierDTOS)){
                Map<Long, List<RfxLineSupplierDTO>> collect = supplierDTOS.stream().collect(Collectors.groupingBy(RfxLineSupplierDTO::getPrLineId));
                for (RfxLineItem rfxLineItem : rfxLineItems) {
                    List<RfxLineSupplierDTO> supplierDTOS1 = collect.get(rfxLineItem.getPrLineId());
                    if(CollectionUtils.isNotEmpty(supplierDTOS1)){
                        for (RfxLineSupplierDTO rfxLineSupplierDTO : supplierDTOS1) {
                            if(null !=supplierItems.get(rfxLineSupplierDTO.getSupplierCompanyId())){
                                List<Long> list = supplierItems.get(rfxLineSupplierDTO.getSupplierCompanyId());
                                list.add(rfxLineItem.getRfxLineItemId());
                            }else {
                                List<Long> rfxItemIds= new ArrayList<>();
                                rfxItemIds.add(rfxLineItem.getRfxLineItemId());
                                supplierItems.put(rfxLineSupplierDTO.getSupplierCompanyId(),rfxItemIds);
                            }
                        }
                    }
                }
            }else {
                for (RfxLineItem rfxLineItem : rfxLineItems) {
                    if(null !=rfxLineItem.getSupplierCompanyId()){
                        if(null !=supplierItems.get(rfxLineItem.getSupplierCompanyId())){
                            List<Long> list = supplierItems.get(rfxLineItem.getSupplierCompanyId());
                            list.add(rfxLineItem.getRfxLineItemId());
                        }else {
                            List<Long> rfxItemIds= new ArrayList<>();
                            rfxItemIds.add(rfxLineItem.getRfxLineItemId());
                            supplierItems.put(rfxLineItem.getSupplierCompanyId(),rfxItemIds);
                        }
                    }
                }
            }
            if(CollectionUtils.isNotEmpty(supplierItems.keySet())){
                List<SourceLineSupplierDTO> sourceLineSupplierDTOList=rfxLineItemRepository.selectInviteSupplier(organizationId,rHeader.getCompanyId(),supplierItems.keySet());
                for (SourceLineSupplierDTO sourceLineSupplierDTO : sourceLineSupplierDTOList) {
                    sourceLineSupplierDTO.setItemIds(supplierItems.get(sourceLineSupplierDTO.getSupplierCompanyId()));
                    sourceLineSupplierDTO.setPrLineSupplierId(sourceLineSupplierDTO.getSupplierCompanyId());
                }
                rfxLineSupplierService.getSupExpirAttachment(rHeader.getTenantId(),rHeader.getCompanyId(),rHeader.getRfxHeaderId(),sourceLineSupplierDTOList);}
        }
        rfxLineItems.forEach(rfxLineItem -> rfxLineItem.setSourceFrom(ShareConstants.SourceCategory.RFX));
        //判断如果物料行存在报价明细,增加物料行的报价明细
//        quotationDetailService.saveQuotationDetails(rfxLineItems);

        // 采购申请批量执行 2019-12-18由发布占用改为保存占用
        rfxLineItemDomainService.batchHoldPrLines(rHeader, rfxLineItems);
        return rHeader;
    }

    private List<RfxHeader> fieldMap(Long tenantId, RfxHeader rfxHeader, List<RfxLineItem> rfxLineItems) {
        Set<Long> lineIds = rfxLineItems.stream().map(line -> line.getPrLineId()).collect(Collectors.toSet());
        // 获取申请数据
        List<PrHeader> prHeaders = spucRemoteService.listPrByLineIds(tenantId, new PrLineDTO(lineIds)).getBody();
        List<RfxHeader> results = new ArrayList<>();
        if (CollectionUtils.isEmpty(prHeaders)) {
            return results;
        }
        for (PrHeader prHeader : prHeaders) {
            List<PrLine> prHeaderLines = prHeader.getPrLineList();
            if (CollectionUtils.isEmpty(prHeaderLines)) {
                continue;
            }
            int j = 0;
            List<PrLine> prLines = new ArrayList<>();
            // 限制批次
            int batch = 10;
            int size = prHeaderLines.size();
            for (PrLine prLine : prHeaderLines) {
                prLines.add(prLine);
                j++;
                if (j % batch == 0 || j == size) {
                    LOGGER.debug("prLines size: " + prLines.size());
                    LOGGER.debug("prLines: " + JSON.toJSONString(prLines));
                    List<RfxHeader> batchResults = batchTranslateData(tenantId, rfxHeader, rfxLineItems, prHeader, prLines);
                    results.addAll(batchResults);
                    prLines = new ArrayList<>();
                }
            }
        }
        return results;
    }

    private List<RfxHeader> batchTranslateData(Long tenantId, RfxHeader rfxHeader, List<RfxLineItem> rfxLineItems, PrHeader prHeader, List<PrLine> prLines) {
        List<RfxHeader> results = new ArrayList<>();
        List<Long> prLineIds = prLines.stream().map(PrLine::getPrLineId).collect(toList());
        List<RfxLineItem> rfxHeaderLines = rfxLineItems.stream().filter(line -> prLineIds.contains(line.getPrLineId())).collect(toList());

        // 获取字段映射模板
        Map<String, String> param = new HashMap(8);
        param.put(ShareConstants.ConfigCenterCode.SiteSsrcRfxFieldMapTemplate.PR_TYPE_NAME, String.valueOf(prHeader.getPrTypeId()));
        param.put(ShareConstants.ConfigCenterCode.SiteSsrcRfxFieldMapTemplate.COMPANY, String.valueOf(prHeader.getCompanyId()));
        param.put(ShareConstants.ConfigCenterCode.SiteSsrcRfxFieldMapTemplate.PURCHASE_ORGANIZATION, String.valueOf(prHeader.getPurchaseOrgId()));
        String fieldMapTemplate = CnfHelper.select(tenantId, ShareConstants.ConfigCenterCode.SiteSsrcRfxFieldMapTemplate.SITE_SSRC_RFX_FIELD_MAP_TEMPLATE, String.class).invokeWithParameter(param);
        LOGGER.debug("fieldMapTemplate " + fieldMapTemplate);
        if (StringUtils.isBlank(fieldMapTemplate)) {
            rfxHeaderLines.forEach(line -> {
                RfxHeader header = new RfxHeader();
                BeanUtils.copyProperties(rfxHeader, header);
                header.setLine(line);
                results.add(header);
            });
            return results;
        }

        // 构建映射参数
        List<Object> request = new ArrayList<>();
        prLines.sort(Comparator.comparing(PrLine::getPrLineId));
        prLines.forEach(line -> {
            PrHeader prRequest = new PrHeader();
            BeanUtils.copyProperties(prHeader, prRequest);
            prRequest.setPrLineList(null);
            prRequest.setPrLine(line);
            request.add(prRequest);
        });

        // 构建被映射参数
        List<Object> response = new ArrayList<>();
        rfxHeaderLines.sort(Comparator.comparing(RfxLineItem::getPrLineId));
        for (RfxLineItem rfxHeaderLine : rfxHeaderLines) {
            RfxHeader header = new RfxHeader();
            BeanUtils.copyProperties(rfxHeader, header);
            header.setLine(rfxHeaderLine);
            response.add(header);
        }
        LOGGER.debug("request: " + JSON.toJSONString(request));
        LOGGER.debug("response: " + JSON.toJSONString(response));
        List<Object> fieldMapResults = AdaptorMappingHelper.batchTranslateData(tenantId, ShareConstants.SsrcSceneCode.SSRC_SCENE_CODE_PR_TO_RFX, fieldMapTemplate, request, response);
        LOGGER.debug("fieldMapResults: " + JSON.toJSONString(fieldMapResults));
        fieldMapResults.forEach(r -> results.add((RfxHeader) r));
        return results;
    }
}
