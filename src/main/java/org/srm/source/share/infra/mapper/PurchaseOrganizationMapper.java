package org.srm.source.share.infra.mapper;

import io.choerodon.mybatis.common.BaseMapper;
import org.srm.source.share.domain.entity.PurchaseOrganization;

/**
 * 采购组织Mapper
 *
 * @author 1@qq.com 2021-08-23 18:59:28
 */
public interface PurchaseOrganizationMapper extends BaseMapper<PurchaseOrganization> {
    PurchaseOrganization selectCodeAndName(Long purchaseOrgId);
}
