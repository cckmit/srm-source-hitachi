package org.srm.source.rfx.app.service;

import org.srm.source.rfx.api.dto.HeaderQueryDTO;
import org.srm.source.rfx.api.dto.RfxCheckItemDTO;

import java.util.List;

/**
 * 报价单行表应用服务
 *
 * @author yuhao.guo@hand-china.com 2019-01-04 17:02:03
 */
public interface HitachiRfxQuotationLineService {

    List<RfxCheckItemDTO> quotationDetail(HeaderQueryDTO headerQueryDTO);
}
