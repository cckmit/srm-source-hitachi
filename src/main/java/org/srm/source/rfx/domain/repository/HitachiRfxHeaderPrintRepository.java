package org.srm.source.rfx.domain.repository;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.srm.source.rfx.api.dto.HitachiRfxPrintDTO;
import org.srm.source.rfx.api.dto.HitachiRfxPrintQueryDTO;

/**
 * @author guotao.yu@hand-china.com 2021/3/22 下午8:56
 */
public interface HitachiRfxHeaderPrintRepository {
    /**
     * 询价单打印列表查询
     * @param hitachiRfxPrintQueryDTO
     * @param pageRequest
     * @return
     */
    Page<HitachiRfxPrintDTO> rfxPrintListQuery(HitachiRfxPrintQueryDTO hitachiRfxPrintQueryDTO, PageRequest pageRequest);
}
