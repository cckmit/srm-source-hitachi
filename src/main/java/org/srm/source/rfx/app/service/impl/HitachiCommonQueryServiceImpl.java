package org.srm.source.rfx.app.service.impl;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.srm.boot.platform.reltable.RelTableHelper;
import org.srm.source.rfx.api.dto.HitachiInvOrganizationDTO;
import org.srm.source.rfx.api.dto.HitachiMallAddressMainDTO;
import org.srm.source.rfx.api.dto.InvOrganizationDTO;
import org.srm.source.share.infra.constant.HitachiConstants;
import org.srm.source.share.infra.repository.impl.HitachiCommonQueryRepositoryImpl;
import org.srm.web.annotation.Tenant;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 * @author tian.yu@going-link.com 2021-05-24 10:44:09
 */
@Service
@Tenant(HitachiConstants.TENANT_NUM)
public class HitachiCommonQueryServiceImpl extends CommonQueryServiceImpl {

    @Autowired
    private HitachiCommonQueryRepositoryImpl hitachiCommonQueryRepository;

    public Page<InvOrganizationDTO> pageAndSortInvOrganizations(PageRequest pageRequest, InvOrganizationDTO queryParam) {

        Page<InvOrganizationDTO> page = hitachiCommonQueryRepository.pageAndSortInvOrganizations(pageRequest, queryParam);

        for (InvOrganizationDTO invOrganizationDTO : page.getContent()) {
            Long tenantId = invOrganizationDTO.getTenantId();
            Long invOrganizationId = invOrganizationDTO.getOrganizationId();
            //查询配置表该库存组织下的主地址
            HitachiMallAddressMainDTO hitachiMallAddressMainDTO = new HitachiMallAddressMainDTO();
            hitachiMallAddressMainDTO.setInvOrganizationId(invOrganizationDTO.getOrganizationId());
            hitachiMallAddressMainDTO.setIsMainAddress("1");
            List<HitachiMallAddressMainDTO> hitachiMallAddressMainDTOS = RelTableHelper.selectByCondition(tenantId, HitachiConstants.ConfigTableCode.SCUX_HITACHI_SMALL_ADDRESS_MAIN, hitachiMallAddressMainDTO);
            //查询地址表该库存组织下的地址表
            List<HitachiInvOrganizationDTO> currentAddressOrganizationDTOS = hitachiCommonQueryRepository.getMallAddressByOrganization(tenantId, invOrganizationId);
            if (CollectionUtils.isNotEmpty(hitachiMallAddressMainDTOS) && CollectionUtils.isNotEmpty(currentAddressOrganizationDTOS)) {
                //检查库存下地址是否存在主地址的list中
                for (HitachiInvOrganizationDTO currentOrganizationDTO : currentAddressOrganizationDTOS) {
                    List<HitachiMallAddressMainDTO> currentAddressMainDTOS = hitachiMallAddressMainDTOS.stream().filter(addressMainDTO -> addressMainDTO.getAddressId().equals(currentOrganizationDTO.getAddressId())).collect(Collectors.toList());
                    if (currentAddressMainDTOS.size() > 0) {
                        invOrganizationDTO.setAddress(currentOrganizationDTO.getAddress());
                        invOrganizationDTO.setPostCode(currentOrganizationDTO.getPostCode());
                        invOrganizationDTO.setMobile(currentOrganizationDTO.getMobile());
                        invOrganizationDTO.setContactName(currentOrganizationDTO.getContactName());
                        invOrganizationDTO.setRegionId(currentOrganizationDTO.getRegionId());
                        invOrganizationDTO.setGoodAddress(currentOrganizationDTO.getGoodAddress());
                        invOrganizationDTO.setLevelPath(hitachiCommonQueryRepository.getLevelPathByRegion(invOrganizationDTO.getRegionId()));
                        break;
                    }
                }
            }
            //根据取到的默认地址查询电商地区表
            if(!StringUtils.isEmpty(invOrganizationDTO.getLevelPath())){
                invOrganizationDTO.setGoodRegion(String.join("",hitachiCommonQueryRepository.getRegionDetailByPath(Arrays.asList(invOrganizationDTO.getLevelPath().split("\\.")))));
            }
        }
        return  page;
    }
}
