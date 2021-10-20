package org.srm.source.share.infra.repository.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.PageHelper;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.srm.source.rfx.api.dto.HitachiInvOrganizationDTO;
import org.srm.source.rfx.api.dto.InvOrganizationDTO;
import org.srm.source.rfx.infra.mapper.HitachiCommonQueryMapper;
import org.srm.source.rfx.infra.repository.impl.CommonQueryRepositoryImpl;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.web.annotation.Tenant;

import java.util.List;

/**
 * 日立查询二开
 *
 * @author tian.yu@going-link.com 2021-05-24 14:40:00
 */
@Component
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiCommonQueryRepositoryImpl extends CommonQueryRepositoryImpl {

    @Autowired
    private HitachiCommonQueryMapper hitachiCommonQueryMapper;

    public Page<InvOrganizationDTO> pageAndSortInvOrganizations(PageRequest pageRequest, InvOrganizationDTO queryParam) {
        return PageHelper.doPageAndSort(pageRequest, () -> hitachiCommonQueryMapper.pageAndSortInvOrganizations(queryParam));
    }

    public List<String> getRegionDetailByPath(List<String>   levePath) {

        return hitachiCommonQueryMapper.getRegionDetailByPath(levePath);
    }

    public List<HitachiInvOrganizationDTO> getMallAddressByOrganization(Long tenantId, Long invOrganizationId) {
        return hitachiCommonQueryMapper.getMallAddressByOrganization(tenantId, invOrganizationId);
    }

    public String getLevelPathByRegion(Long regionId) {
        return hitachiCommonQueryMapper.getLevelPathByRegion(regionId);
    }

}
