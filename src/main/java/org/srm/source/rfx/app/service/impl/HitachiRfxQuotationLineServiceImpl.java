package org.srm.source.rfx.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.srm.boot.platform.configcenter.CnfHelper;
import org.srm.source.rfx.api.dto.HeaderQueryDTO;
import org.srm.source.rfx.api.dto.RfxCheckItemDTO;
import org.srm.source.rfx.api.dto.RfxLineItemDTO;
import org.srm.source.rfx.api.dto.RfxQuotationLineDTO;
import org.srm.source.rfx.app.service.HitachiRfxQuotationLineService;
import org.srm.source.rfx.domain.entity.RfxHeader;
import org.srm.source.rfx.domain.entity.RfxLineItem;
import org.srm.source.rfx.domain.entity.RfxQuotationLine;
import org.srm.source.rfx.domain.repository.RfxHeaderRepository;
import org.srm.source.rfx.domain.repository.RfxQuotationLineRepository;
import org.srm.source.rfx.infra.constant.SourceConstants;
import org.srm.source.rfx.infra.mapper.HitachiRfxHeaderMapper;
import org.srm.source.share.app.service.HitachiQuotaTaxService;
import org.srm.source.share.domain.entity.SourceTemplate;
import org.srm.source.share.domain.repository.SourceTemplateRepository;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.constant.ShareConstants;
import org.srm.web.annotation.Tenant;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 报价单行表应用服务默认实现
 *
 * @author junjie.liu01@hand-china.com
 */
@Service
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiRfxQuotationLineServiceImpl extends RfxQuotationLineServiceImpl implements HitachiRfxQuotationLineService {

    @Autowired
    private HitachiRfxHeaderMapper rfxHeaderMapper;

    @Autowired
    private HitachiQuotaTaxService quotaTaxService;
    @Autowired
    private RfxHeaderRepository rfxHeaderRepository;
    @Autowired
    private SourceTemplateRepository sourceTemplateRepository;
    @Autowired
    private RfxQuotationLineRepository rfxQuotationLineRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @ProcessLovValue
    public Page<RfxQuotationLine> rfxSupplierQuotationSave(Long tenantId, List<RfxQuotationLine> rfxQuotationLineList) {
        // 查出 公司，物料id，物料类别
        List<Long> quotationLineIds = rfxQuotationLineList.stream().map(RfxQuotationLine::getQuotationLineId).collect(Collectors.toList());
        List<RfxQuotationLine> rfxQuotationLinesDb = rfxHeaderMapper.selectByQuotationLineIds(quotationLineIds);
        RfxQuotationLine rfxQuotationLineOneDb = rfxQuotationLinesDb.get(0);
        Map<Long, RfxQuotationLine> rfxQuotationLineMap = rfxQuotationLinesDb.stream().collect(Collectors.toMap(RfxQuotationLine::getQuotationLineId, e -> e));
        rfxQuotationLineList.forEach(e -> {
            //将查出的数据库字段（物料id，物料类别）赋值
            e.setItemId(rfxQuotationLineMap.get(e.getQuotationLineId()).getItemId());
            e.setItemCategoryId(rfxQuotationLineMap.get(e.getQuotationLineId()).getItemCategoryId());
            if (Objects.isNull(e.getUomId())) {
                e.setUomId(rfxQuotationLineMap.get(e.getQuotationLineId()).getUomId());
            }
            //总账科目
            e.setAttributeBigint19(rfxQuotationLineMap.get(e.getQuotationLineId()).getAttributeBigint19());
        });

        //设置业务规则定义需要的参数
        Map<String, String> cnfArgs = new HashMap<>(2);
        cnfArgs.put("companyId", String.valueOf(rfxQuotationLineOneDb.getCompanyId()));
        this.handleTaxCalculation(tenantId, cnfArgs, rfxQuotationLineList);

        return super.rfxSupplierQuotationSave(tenantId, rfxQuotationLineList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertOrUpdateOffLineQuotation(Long tenantId, List<RfxQuotationLineDTO> quotationLineDTOList) {
        //不含税赋值
        quotationLineDTOList.stream().filter(e -> Objects.nonNull(e.getAttributeDecimal1()))
                .forEach(e -> {
                    e.setCurrentQuotationPrice(e.getAttributeDecimal1());
                    e.setNetPrice(e.getAttributeDecimal1());
                });
        // 查出 公司，物料id，物料类别
        List<Long> rfxLineItemIds = quotationLineDTOList.stream().map(RfxQuotationLineDTO::getRfxLineItemId).collect(Collectors.toList());
        List<RfxLineItemDTO> rfxLineItemsDb = rfxHeaderMapper.selectByRfxLineItemIds(rfxLineItemIds);
        RfxLineItemDTO rfxLineItemOneDb = rfxLineItemsDb.get(0);
        Map<Long, RfxLineItemDTO> rfxLineItemsMap = rfxLineItemsDb.stream().collect(Collectors.toMap(RfxLineItemDTO::getRfxLineItemId, e -> e));
        quotationLineDTOList.forEach(e -> {
            //将查出的数据库字段（物料id，物料类别）赋值
            e.setItemId(rfxLineItemsMap.get(e.getRfxLineItemId()).getItemId());
            e.setItemCategoryId(rfxLineItemsMap.get(e.getRfxLineItemId()).getItemCategoryId());
            if (Objects.isNull(e.getUomId())) {
                e.setUomId(rfxLineItemsMap.get(e.getRfxLineItemId()).getUomId());
            }
            if (StringUtils.isBlank(e.getQuotationCurrencyCode())) {
                e.setQuotationCurrencyCode(rfxLineItemsMap.get(e.getRfxLineItemId()).getCurrencyCode());
            }
            //总账科目
            e.setAttributeBigint19(rfxLineItemsMap.get(e.getRfxLineItemId()).getAttributeBigint19());
        });

        //设置业务规则定义需要的参数
        Map<String, String> cnfArgs = new HashMap<>(2);
        cnfArgs.put("companyId", String.valueOf(rfxLineItemOneDb.getCompanyId()));
        this.handleTaxCalculationOff(tenantId, cnfArgs, quotationLineDTOList);

        super.insertOrUpdateOffLineQuotation(tenantId, quotationLineDTOList);
    }


    @Override
    public void preCalculationOfQuotation(RfxQuotationLine rfxQuotationLine, String priceTypeCode) {
        RfxQuotationLine rfxQuotationLineDb = rfxHeaderMapper.selectByQuotationLineIds(Collections.singletonList(rfxQuotationLine.getQuotationLineId())).get(0);
        rfxQuotationLine.setItemId(rfxQuotationLineDb.getItemId());
        rfxQuotationLine.setItemCategoryId(rfxQuotationLineDb.getItemCategoryId());
        if (Objects.isNull(rfxQuotationLine.getUomId())) {
            rfxQuotationLine.setUomId(rfxQuotationLineDb.getUomId());
        }
        if (Objects.isNull(rfxQuotationLine.getCurrencyCode())) {
            rfxQuotationLine.setCurrencyCode(rfxQuotationLineDb.getCurrencyCode());
        }
        //总账科目
        rfxQuotationLine.setAttributeBigint19(rfxQuotationLineDb.getAttributeBigint19());

        //设置业务规则定义需要的参数
        Map<String, String> cnfArgs = new HashMap<>(2);
        cnfArgs.put("companyId", String.valueOf(rfxQuotationLineDb.getCompanyId()));
        boolean result = this.handleTaxCalculation(rfxQuotationLineDb.getTenantId(), cnfArgs, Collections.singletonList(rfxQuotationLine));
        if (!result) {
            //如果未执行日本税率则执行原先逻辑
            super.preCalculationOfQuotation(rfxQuotationLine, priceTypeCode);
        }
    }

    /**
     * 报价行对应关系 - 保存
     *
     * @param tenantId
     * @param rfxQuotationLines
     * @return
     */
    private boolean handleTaxCalculation(Long tenantId, Map<String, String> cnfArgs, List<RfxQuotationLine> rfxQuotationLines) {
        return quotaTaxService.handleQuotaTaxCalculation(tenantId, cnfArgs, rfxQuotationLines,
                (taxDto, line) -> {
                    // 提供给 QuotaTaxDTO 处理
                    taxDto.setId(line.getQuotationLineId());
                    taxDto.setUnitPrice(line.getAttributeDecimal1());
                    taxDto.setQuantity(line.getCurrentQuotationQuantity());
                    taxDto.setUomId(line.getUomId());
                    taxDto.setTaxId(line.getTaxId());
                    taxDto.setCurrencyCode(line.getCurrencyCode());
                    taxDto.setItemId(line.getItemId());
                    taxDto.setCategoryId(line.getItemCategoryId());
                    taxDto.setAccountSubjectId(line.getAttributeBigint19());
                },
                (taxDto, line) -> {
                    // QuotaTaxDTO 回写
                    line.setCurrentQuotationPrice(taxDto.getIncludeTaxUnitPrice());
                    line.setNetPrice(taxDto.getExcludeTaxUnitPrice());
                    line.setAttributeDecimal2(taxDto.getConsumeTaxAmount());
                    line.setAttributeDecimal3(taxDto.getQuotaTaxAmount());
                    line.setTotalAmount(taxDto.getIncludeTaxAmount());
                    line.setNetAmount(taxDto.getExcludeTaxAmount());
                });
    }

    private boolean handleTaxCalculationOff(Long tenantId, Map<String, String> cnfArgs, List<RfxQuotationLineDTO> rfxQuotationLines) {
        return quotaTaxService.handleQuotaTaxCalculation(tenantId, cnfArgs, rfxQuotationLines,
                (taxDto, line) -> {
                    // 提供给 QuotaTaxDTO 处理
                    taxDto.setId(line.getQuotationLineId());
                    taxDto.setUnitPrice(line.getAttributeDecimal1());
                    taxDto.setQuantity(line.getCurrentQuotationQuantity());
                    taxDto.setUomId(line.getUomId());
                    taxDto.setTaxId(line.getTaxId());
                    taxDto.setCurrencyCode(line.getQuotationCurrencyCode());
                    taxDto.setItemId(line.getItemId());
                    taxDto.setCategoryId(line.getItemCategoryId());
                    taxDto.setAccountSubjectId(line.getAttributeBigint19());
                },
                (taxDto, line) -> {
                    // QuotaTaxDTO 回写
                    line.setCurrentQuotationPrice(taxDto.getIncludeTaxUnitPrice());
                    line.setNetPrice(taxDto.getExcludeTaxUnitPrice());
                    line.setAttributeDecimal2(taxDto.getConsumeTaxAmount());
                    line.setAttributeDecimal3(taxDto.getQuotaTaxAmount());
                    line.setTotalAmount(taxDto.getIncludeTaxAmount());
                    line.setNetAmount(taxDto.getExcludeTaxAmount());
                });
    }

    @Override
    public List<RfxCheckItemDTO> quotationDetail(HeaderQueryDTO headerQueryDTO) {
        // 获取基准价的配置
        RfxHeader rfxHeader = rfxHeaderRepository.selectByPrimaryKey(headerQueryDTO.getRfxHeaderId());
        SourceTemplate sourceTemplate = sourceTemplateRepository.selectBySourceTemplateId(rfxHeader.getTemplateId());
        Map<String, String> parameter = new HashMap<>(BaseConstants.Digital.TWO);
        parameter.put("company", rfxHeader.getCompanyId() == null ? null : rfxHeader.getCompanyId().toString());
        parameter.put("sourceCategory", rfxHeader.getSourceCategory());
        parameter.put("sourceTemplate", sourceTemplate.getTemplateNum());
        String priceTypeCode = CnfHelper.select(headerQueryDTO.getTenantId(), ShareConstants.ConfigCenterCode.QUOTATION_SET, String.class).invokeWithParameter(parameter);
        headerQueryDTO.setRankRule(sourceTemplate.getRankRule());
        headerQueryDTO.setSourceMethod(rfxHeader.getSourceMethod());
        headerQueryDTO.setPriceTypeCode(StringUtils.isBlank(priceTypeCode) ? SourceConstants.PriceTypeCode.TAX_INCLUDED_PRICE : priceTypeCode);
        headerQueryDTO.setAuctionDirection(rfxHeader.getAuctionDirection());
        List<RfxCheckItemDTO> rfxCheckItemDTOS = rfxQuotationLineRepository.selectQuotationDetail(headerQueryDTO);

        return rfxCheckItemDTOS;
    }

}
