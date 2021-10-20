package org.srm.source.rfx.infra.mapper;

import io.choerodon.core.domain.Page;
import org.apache.ibatis.annotations.Param;
import org.srm.source.share.domain.entity.PriceAppScopeLine;
import org.srm.source.rfx.api.dto.HitachiInvOrganizationDTO;
import org.srm.source.rfx.api.dto.HitachiOperationUnitDTO;
import org.srm.source.rfx.api.dto.InvOrganizationDTO;

import java.util.List;

/**
 *
 * @author tian.yu@going-link.com  2021/05/24 13:40
 */
public interface HitachiCommonQueryMapper {

    /**
     * 分页查询库存组织及扩展信息
     *
     * @param queryParam 查询条件
     * @return 列表数据
     */
    Page<InvOrganizationDTO> pageAndSortInvOrganizations(InvOrganizationDTO queryParam);

    /**
     * 通过区域路径获取区域拼接的地址信息
     * @param levelPath
     * @return
     */
    List<String> getRegionDetailByPath(@Param("levelPaths") List<String>  levelPath);

    /**
     * 查询库存组织及扩展信息
     *
     * @param queryParam 查询条件
     * @return 列表数据
     */
    List<InvOrganizationDTO> selectInvOrganizations(HitachiInvOrganizationDTO queryParam);

    /**
     * 查询业务实体及扩展信息
     *
     * @param queryParam 查询条件
     * @return 列表数据
     */
    List<HitachiOperationUnitDTO> selectOperationUnitsGG(HitachiOperationUnitDTO queryParam);
    List<HitachiOperationUnitDTO> selectOperationUnitsSY(HitachiOperationUnitDTO queryParam);
    List<HitachiOperationUnitDTO> selectOperationUnitsST(HitachiOperationUnitDTO queryParam);

    List<PriceAppScopeLine> selectCompanysByIds(@Param("tenantId") Long tenantId, @Param("companyIds") List<Long> companyIds);
    List<PriceAppScopeLine> selectOperationUnitsByIds(@Param("tenantId") Long tenantId, @Param("ouIds") List<Long> ouIds);
    List<PriceAppScopeLine> selectInvOrganizationsByIds(@Param("tenantId") Long tenantId, @Param("invOrganizationIds") List<Long> invOrganizationIds);
    List<PriceAppScopeLine> selectPurOrganizationsByIds(@Param("tenantId") Long tenantId, @Param("purOrganizationIds") List<Long> purOrganizationIds);

    List<HitachiOperationUnitDTO> selectAllComOuInv(@Param("tenantId") Long tenantId, @Param("invOrganizationIds") List<Long> invOrganizationIds);


    List<HitachiInvOrganizationDTO> getMallAddressByOrganization(@Param("tenantId") Long tenantId,@Param("invOrganizationId") Long invOrganizationId);

    String getLevelPathByRegion(@Param("regionId") Long regionId);

}
