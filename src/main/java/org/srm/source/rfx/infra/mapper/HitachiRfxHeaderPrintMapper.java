package org.srm.source.rfx.infra.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.srm.source.rfx.api.dto.*;

/**
 * @author guotao.yu@hand-china.com 2021/3/22 下午9:02
 */
public interface HitachiRfxHeaderPrintMapper {
    /**
     * 询价单打印列表查询
     *
     * @param hitachiRfxPrintQueryDTO
     * @return
     */
    List<HitachiRfxPrintDTO> rfxPrintListQuery(HitachiRfxPrintQueryDTO hitachiRfxPrintQueryDTO);

    /**
     * 根据用户查询所属部门
     *
     * @param tenantId
     * @param userId
     * @return
     */
    List<HitachiUnit> selectUserOwnDepartment(@Param("tenantId") Long tenantId, @Param("userId") Long userId);

    /**
     * 根据部门查询部门
     *
     * @param tenantId
     * @param unitId
     * @return
     */
    HitachiUnit selectUnit(@Param("tenantId") Long tenantId, @Param("unitId") Long unitId);

    /**
     * 获取业务实体的telephone
     *
     * @param tenantId
     * @param rfxHeaderId
     * @return
     */
    String invOrganizationTelQuery(@Param("tenantId") Long tenantId, @Param("rfxHeaderId") Long rfxHeaderId);

    /**
     * 查询业务实体对应的号码
     * @param tenantId
     * @param ouId
     * @return
     */
    String selectOuTelPhone(@Param("tenantId") Long tenantId, @Param("ouId") Long ouId);

    /**
     * 打印行查询
     *
     * @param tenantId
     * @param rfxHeaderId
     * @return
     */
    List<HitachiRfxLineItemPrintDTO> rfxLineItemPrintQuery(@Param("tenantId") Long tenantId, @Param("rfxHeaderId") Long rfxHeaderId);

    /**
     * 获取多语言币种
     *
     * @param tenantId
     * @param currencyCode
     * @return
     */
    String selectValidCurrency(Long tenantId, String currencyCode);

    /**
     * 根据供应商联系人主键查询联系人
     *
     * @param supplierContactId:
     * @return org.srm.source.rfx.api.dto.HitachiCompanyContactDTO
     * @author junheng.duan@going-link.com 2021-05-26 15:39
     */
    HitachiSupplierContactDTO selectContactByPrimaryKey(@Param("supplierContactId") Long supplierContactId);

    /**
     * 根据询价单头id查询送达方
     *
     * @param tenantId:
     * @param rfxHeaderId:
     * @author junheng.duan@going-link.com 2021-06-29 14:21
     * @return java.util.List<org.srm.source.rfx.api.dto.HitachiRfxConsigneeDTO>
     */
    List<HitachiRfxConsigneeDTO> rfxConsigneeQuery(@Param("tenantId") Long tenantId, @Param("rfxHeaderId") Long rfxHeaderId);
}
