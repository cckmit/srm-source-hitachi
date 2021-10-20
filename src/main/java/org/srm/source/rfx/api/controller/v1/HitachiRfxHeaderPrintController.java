package org.srm.source.rfx.api.controller.v1;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.base.BaseConstants;
import org.hzero.core.util.Results;
import org.hzero.starter.keyencrypt.core.Encrypt;
import org.hzero.starter.keyencrypt.core.IEncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.srm.common.annotation.FilterSupplier;
import org.srm.source.rfx.api.dto.HitachiRfxConsigneeDTO;
import org.srm.source.rfx.api.dto.HitachiRfxPrintDTO;
import org.srm.source.rfx.api.dto.HitachiRfxPrintQueryDTO;
import org.srm.source.rfx.api.dto.HitachiSelectedPrintDTO;
import org.srm.source.rfx.app.service.HitachiRfxHeaderPrintService;
import org.srm.source.rfx.config.HitachiSourceSwaggerApiConfig;
import org.srm.source.rfx.domain.entity.RfxHeader;
import springfox.documentation.annotations.ApiIgnore;

/**
 * 询价单打印导出-日立物流
 * @author guotao.yu@hand-china.com 2021/3/22 下午8:11
 */
@Api(tags = HitachiSourceSwaggerApiConfig.HITACHI_SOURCE_RFX_PRINT)
@RestController("hitachiRfxHeaderPrintController.v1")
@RequestMapping("/v1/{organizationId}/scux-hitachi/rfx/print")
public class HitachiRfxHeaderPrintController {
    @Autowired
    private HitachiRfxHeaderPrintService hitachiRfxHeaderPrintService;
    @Autowired
    private IEncryptionService iEncryptionService;

    @ApiOperation(value = "询价单打印列表查询-日立物流")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @GetMapping("/list-query")
    @ProcessLovValue(targetField = BaseConstants.FIELD_BODY)
    @FilterSupplier
    public ResponseEntity<Page<HitachiRfxPrintDTO>> hitachiRfxPrintListQuery(@PathVariable Long organizationId,
                                                                             @Encrypt HitachiRfxPrintQueryDTO hitachiRfxPrintQueryDTO,
                                                                             @ApiIgnore @SortDefault(value = RfxHeader.FIELD_RFX_NUM,
                                                                    direction = Sort.Direction.DESC) PageRequest pageRequest) {
        hitachiRfxPrintQueryDTO.setTenantId(organizationId);
        return Results.success(hitachiRfxHeaderPrintService.rfxPrintListQuery(hitachiRfxPrintQueryDTO, pageRequest));
    }

    @ApiOperation(value = "询价单打印打包下载-日立物流")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/list-download")
    @FilterSupplier
    public ResponseEntity postHitachiRfxPrintDownload(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  @PathVariable("organizationId") Long tenantId,
                                                  @ApiParam(value = "询价单信息") @RequestBody(required = true) @Encrypt HitachiSelectedPrintDTO hitachiSelectedPrintDTO) {
        hitachiRfxHeaderPrintService.postRfxPrintPackDownload(request, response,tenantId, hitachiSelectedPrintDTO);
        return Results.success();
    }

    @ApiOperation(value = "询价单打印校验-日立物流")
    @Permission(level = ResourceLevel.ORGANIZATION)
    @PostMapping("/list-download-validate")
    @FilterSupplier
    public ResponseEntity<List<HitachiRfxConsigneeDTO>> hitachiRfxPrintValidate(@PathVariable("organizationId") Long tenantId,
                                                                                @RequestBody(required = true) @Encrypt List<HitachiRfxPrintDTO> hitachiRfxPrintDTOList) {
        return Results.success(hitachiRfxHeaderPrintService.hitachiRfxPrintValidate(tenantId, hitachiRfxPrintDTOList));
    }
}
