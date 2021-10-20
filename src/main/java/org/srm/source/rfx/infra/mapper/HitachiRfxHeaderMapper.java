package org.srm.source.rfx.infra.mapper;

import org.apache.ibatis.annotations.Param;
import org.srm.source.rfx.api.dto.RfxLineItemDTO;
import org.srm.source.rfx.domain.entity.RfxQuotationLine;

import java.util.List;

/**
 * 寻源头
 *
 * @author liujunjie
 */
public interface HitachiRfxHeaderMapper {
    /**
     * 查询报价行
     *
     * @param quotationLineIds
     * @return
     */
    List<RfxQuotationLine> selectByQuotationLineIds(@Param("quotationLineIds") List<Long> quotationLineIds);

    /**
     * 查询物料行
     *
     * @param rfxLineItemIds
     * @return
     */
    List<RfxLineItemDTO> selectByRfxLineItemIds(@Param("rfxLineItemIds")List<Long> rfxLineItemIds);
}
