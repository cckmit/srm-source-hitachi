package org.srm.source.rfx.infra.mapper;

import org.apache.ibatis.annotations.Param;
import org.srm.source.rfx.api.dto.RfxQuotationHeaderDTO;

/**
 * 报价单头表Mapper
 *
 * @author yuhao.guo@hand-china.com 2019-01-04 17:02:03
 */
public interface HitachiRfxQuotationHeaderMapper {

    /**
     * 供应报价单头查询
     * @param tenantId  租户id
     * @param rfxQuotationHeaderId
     * @param supplierTenantId
     * @return  RfxQuotationHeaderDTO
     */
    RfxQuotationHeaderDTO selectQuotationHeaderByRfxQuotationHeaderId(@Param("tenantId") Long tenantId, @Param("rfxQuotationHeaderId") Long rfxQuotationHeaderId,@Param("supplierTenantId") Long supplierTenantId);

}
