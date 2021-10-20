package org.srm.source.share.infra.repository.impl;

import org.hzero.mybatis.base.impl.BaseRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.srm.source.share.domain.entity.PurchaseOrganization;
import org.srm.source.share.domain.repository.PurchaseOrganizationRepository;
import org.springframework.stereotype.Component;
import org.srm.source.share.infra.mapper.PurchaseOrganizationMapper;
import org.srm.web.annotation.Tenant;

/**
 * 采购组织 资源库实现
 *
 * @author 1@qq.com 2021-08-23 18:59:28
 */
@Component
@Tenant
public class PurchaseOrganizationRepositoryImpl extends BaseRepositoryImpl<PurchaseOrganization> implements PurchaseOrganizationRepository {
    @Autowired
    private PurchaseOrganizationMapper purchaseOrganizationMapper;

    @Override
    public PurchaseOrganization selectCodeAndName(Long purchaseOrgId) {
        return purchaseOrganizationMapper.selectCodeAndName(purchaseOrgId);
    }
}
