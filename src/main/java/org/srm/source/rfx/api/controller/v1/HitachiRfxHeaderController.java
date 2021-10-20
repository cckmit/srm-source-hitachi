package org.srm.source.rfx.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.collections.CollectionUtils;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.base.BaseController;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.srm.common.annotation.FilterSupplier;
import org.srm.source.rfx.api.dto.HeaderQueryDTO;
import org.srm.source.rfx.api.dto.InvOrganizationDTO;
import org.srm.source.rfx.api.dto.RfxHeaderDTO;
import org.srm.source.rfx.api.dto.RfxQuotationLineDTO;
import org.srm.source.rfx.app.service.RfxHeaderService;
import org.srm.source.rfx.app.service.impl.HitachiCommonQueryServiceImpl;
import org.srm.source.rfx.config.SourceSwaggerApiConfig;
import org.srm.source.rfx.domain.entity.RfxHeader;
import org.srm.source.rfx.domain.entity.RfxQuotationLine;
import org.srm.source.share.app.service.HitachiQuotaTaxService;
import org.srm.source.share.infra.annotation.PrecisionSwitch;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.web.annotation.Tenant;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 询价单头表 管理 API
 *
 * @author xuan.zhang03@hand-china.com 2018-12-27 18:44:58
 */
@Api(tags = SourceSwaggerApiConfig.SOURCE_RFX_HEADER)
@RestController("hitachiRfxHeaderController.v1")
@RequestMapping("/v1/{organizationId}/rfx")
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiRfxHeaderController extends BaseController {

    @Autowired
    private RfxHeaderService rfxHeaderService;

    @Autowired
    private HitachiQuotaTaxService hitachiQuotaTaxService;

    @Autowired
    private HitachiCommonQueryServiceImpl hitachiCommonQueryService;

    @ApiOperation(value = "还比价查询")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/bargain")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @FilterSupplier
    @PrecisionSwitch
    public ResponseEntity<Page<RfxQuotationLineDTO>> detailBargain(@PathVariable Long organizationId,
                                                                   @Encrypt RfxQuotationLine rfxBargain,
                                                                   PageRequest pageRequest) {
        Page<RfxQuotationLineDTO> rfxQuotationLinesPage = rfxHeaderService.detailBargain(organizationId, rfxBargain, pageRequest);
        if (CollectionUtils.isNotEmpty(rfxQuotationLinesPage)) {
            //设置是否启用日本税率
            Integer japanTaxFlag = hitachiQuotaTaxService.getJapanTaxFlag(organizationId, rfxQuotationLinesPage.get(0).getCompanyId());
            rfxQuotationLinesPage.getContent().forEach(e -> e.setAttributeTinyint1(japanTaxFlag));

        }
        return Results.success(rfxQuotationLinesPage);
    }

    @ApiOperation(value = "询价单头表明细")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/{rfxHeaderId}")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @FilterSupplier
    public ResponseEntity<RfxHeaderDTO> detailRfxHeader(@PathVariable Long organizationId, @Encrypt @PathVariable Long rfxHeaderId) {
        RfxHeaderDTO rfxHeaderDTO = rfxHeaderService.selectOneRfxHeader(new HeaderQueryDTO(rfxHeaderId, organizationId));
        //设置是否启用日本税率
        Integer japanTaxFlag = hitachiQuotaTaxService.getJapanTaxFlag(organizationId, rfxHeaderDTO.getCompanyId());
        rfxHeaderDTO.setAttributeTinyint1(japanTaxFlag);
        return Results.success(rfxHeaderDTO);
    }

    @ApiOperation(value = "供应商询价单头查询")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/supplier/{rfxHeaderId}/header")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    public ResponseEntity<RfxHeaderDTO> supplierRfxHeader(@PathVariable("organizationId") Long tenantId, @PathVariable @Encrypt Long rfxHeaderId, @Encrypt RfxHeader rfxHeader) {
        RfxHeaderDTO rfxHeaderDTO = rfxHeaderService.selectSupplierRfxHeader(tenantId, rfxHeaderId, rfxHeader);
        //设置是否启用日本税率
        Integer japanTaxFlag = hitachiQuotaTaxService.getJapanTaxFlag(tenantId, rfxHeaderDTO.getCompanyId());
        rfxHeaderDTO.setAttributeTinyint1(japanTaxFlag);
        return Results.success(rfxHeaderDTO);
    }

    @GetMapping("/inv-organizations")
    @ApiOperation("查询库存组织，用于物料行选择库存组织")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @FilterSupplier
    public ResponseEntity<Page<InvOrganizationDTO>> list(@ApiParam(value = "租户ID", required = true) @PathVariable("organizationId") Long tenantId, @Encrypt InvOrganizationDTO invOrganization, @ApiIgnore @SortDefault(value = {"organizationCode"}, direction = Sort.Direction.ASC) PageRequest pageRequest) {
        invOrganization.setTenantId(tenantId);
        Page<InvOrganizationDTO> list = hitachiCommonQueryService.pageAndSortInvOrganizations(pageRequest, invOrganization);
        // 组件地址区域
        return Results.success(list);
    }

}
