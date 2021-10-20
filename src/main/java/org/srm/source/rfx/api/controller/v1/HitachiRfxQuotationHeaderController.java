package org.srm.source.rfx.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.srm.source.rfx.api.dto.RfxQuotationHeaderDTO;
import org.srm.source.rfx.api.dto.RfxSelectQuotationLineDTO;
import org.srm.source.rfx.app.service.RfxQuotationHeaderService;
import org.srm.source.rfx.app.service.RfxQuotationLineService;
import org.srm.source.rfx.config.SourceSwaggerApiConfig;
import org.srm.source.rfx.domain.entity.RfxQuotationLine;
import org.srm.source.share.app.service.HitachiQuotaTaxService;
import org.srm.source.share.infra.annotation.PrecisionSwitch;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.web.annotation.Tenant;

/**
 * 报价单头表 管理 API
 *
 * @author yuhao.guo@hand-china.com 2019-01-04 17:02:03
 */
@Api(tags = SourceSwaggerApiConfig.SUPPLIER_RFX_QUOTATION)
@RestController("hitachiRfxQuotationHeaderController.v1")
@RequestMapping("/v1/{organizationId}/rfx/quotation")
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiRfxQuotationHeaderController {

    @Autowired
    private RfxQuotationLineService rfxQuotationLineService;

    @Autowired
    private RfxQuotationHeaderService rfxQuotationHeaderService;

    @Autowired
    private HitachiQuotaTaxService hitachiQuotaTaxService;

    @ApiOperation(value = "供应商报价-提交API")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/submit")
    public ResponseEntity<Page<RfxQuotationLine>> submit(@PathVariable("organizationId") Long tenantId, @RequestBody @Encrypt RfxSelectQuotationLineDTO rfxSelectQuotationLine) {
        //单价同步赋值 - 防止校验为空，处理完单价后会，重新复制
        if (BaseConstants.Flag.YES.equals(rfxSelectQuotationLine.getRfxQuotationHeader().getAttributeTinyint1())) {
            rfxSelectQuotationLine.getRfxQuotationLines().forEach(e -> e.setCurrentQuotationPrice(e.getAttributeDecimal1()));
        }
        return Results.success(rfxQuotationLineService.quotationSubmit(tenantId, rfxSelectQuotationLine));
    }

    @ApiOperation(value = "供应商报价-报价单头查询")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{quotationHeaderId}/header")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @PrecisionSwitch
    public ResponseEntity<RfxQuotationHeaderDTO> selectQuotationHeaderByRfxQuotationHeaderId(@PathVariable("organizationId") Long tenantId, @PathVariable("quotationHeaderId") @Encrypt Long rfxQuotationHeaderId) {
        RfxQuotationHeaderDTO rfxQuotationHeaderDTO = rfxQuotationHeaderService.selectQuotationHeaderByRfxQuotationHeaderId(tenantId, rfxQuotationHeaderId);
        // 查询添加额外字段
        rfxQuotationHeaderDTO.setAttributeTinyint1(hitachiQuotaTaxService.getJapanTaxFlag(tenantId, rfxQuotationHeaderDTO.getCompanyId()));
        return Results.success(rfxQuotationHeaderDTO);
    }

}
