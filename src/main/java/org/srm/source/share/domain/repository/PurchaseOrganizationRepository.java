package org.srm.source.share.domain.repository;

import org.hzero.mybatis.base.BaseRepository;
import org.srm.source.share.domain.entity.PurchaseOrganization;

/**
 * 采购组织资源库
 *
 *
 */
public interface PurchaseOrganizationRepository extends BaseRepository<PurchaseOrganization> {
    PurchaseOrganization selectCodeAndName(Long purchaseOrgId);
}
