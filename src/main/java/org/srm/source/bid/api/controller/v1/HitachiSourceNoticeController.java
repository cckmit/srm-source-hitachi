package org.srm.source.bid.api.controller.v1;

import io.choerodon.core.domain.Page;
import io.choerodon.core.iam.ResourceLevel;
import io.choerodon.mybatis.pagehelper.annotation.SortDefault;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import io.choerodon.mybatis.pagehelper.domain.Sort;
import io.choerodon.swagger.annotation.Permission;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hzero.boot.platform.lov.annotation.ProcessLovValue;
import org.hzero.core.util.Results;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.srm.source.bid.app.service.HitachiSourceNoticeService;
import org.srm.source.bid.app.service.SourceNoticeService;
import org.srm.source.bid.domain.entity.PubSourceNotice;
import org.srm.source.bid.domain.entity.SourceNotice;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.web.annotation.Tenant;


@RestController("hitachiSourceNoticeController.v1")
@RequestMapping({"/v1/{organizationId}/source-notices"})
@Api(
        tags = {"SourceNotice"}
)
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiSourceNoticeController {

    @Autowired
    private SourceNoticeService sourceNoticeService;
    @Autowired
    private HitachiSourceNoticeService hitachiSourceNoticeService;


    @ApiOperation("招标寻源公告列表查询(登录时调用)")
    @Permission(
            permissionLogin = true,
            level = ResourceLevel.ORGANIZATION
    )
    @GetMapping({"/br-list"})
    @ProcessLovValue(
            targetField = {"body"}
    )
    public ResponseEntity<Page<PubSourceNotice>> listBRSourceNotice(@PathVariable String organizationId, String bidTitle, String sourceFrom, String lang, @SortDefault(value = {"noticeId"},direction = Sort.Direction.DESC) PageRequest pageRequest) {
        Page<SourceNotice> list = this.sourceNoticeService.listSourceNotice(pageRequest, organizationId, bidTitle, "BR", sourceFrom);
        return Results.success(this.hitachiSourceNoticeService.processLovTranslate(organizationId, lang, list));
    }
}
