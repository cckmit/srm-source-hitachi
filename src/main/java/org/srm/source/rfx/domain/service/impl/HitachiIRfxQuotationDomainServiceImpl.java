package org.srm.source.rfx.domain.service.impl;

import io.choerodon.core.exception.CommonException;
import org.hzero.core.base.BaseConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.srm.source.rfx.domain.entity.RfxQuotationLine;
import org.srm.source.rfx.domain.service.IRfxQuotationDomainService;
import org.srm.source.rfx.infra.mapper.HitachiRfxHeaderMapper;
import org.srm.source.share.app.service.HitachiQuotaTaxService;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.web.annotation.Tenant;

import java.util.*;

/**
 * 报价单行表
 *
 * @author junjie.liu01@hand-china.com
 */
@Component
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiIRfxQuotationDomainServiceImpl extends IRfxQuotationDomainServiceImpl implements IRfxQuotationDomainService {

    @Autowired
    private HitachiRfxHeaderMapper rfxHeaderMapper;

    @Autowired
    private HitachiQuotaTaxService quotaTaxService;

    @Override
    public void calculationOfQuotation(RfxQuotationLine rfxQuotationLine, String priceTypeCode) {
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
            super.calculationOfQuotation(rfxQuotationLine, priceTypeCode);
        }
    }

    /**
     * 报价行对应关系
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
                    // 提交赋值有效字段
                    line.setValidQuotationPrice(line.getCurrentQuotationPrice());
                    line.setAttributeDecimal4(line.getAttributeDecimal1());
                    line.setValidNetPrice(line.getNetPrice());
                });
    }


}
