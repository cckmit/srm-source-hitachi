package org.srm.source.rfx.app.service;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import io.choerodon.core.domain.Page;
import io.choerodon.mybatis.pagehelper.domain.PageRequest;
import org.srm.source.rfx.api.dto.HitachiRfxConsigneeDTO;
import org.srm.source.rfx.api.dto.HitachiRfxPrintDTO;
import org.srm.source.rfx.api.dto.HitachiRfxPrintQueryDTO;
import org.srm.source.rfx.api.dto.HitachiSelectedPrintDTO;

/**
 * @author guotao.yu@hand-china.com 2021/3/22 下午8:39
 */
public interface HitachiRfxHeaderPrintService {
    /**
     * 询价单打印列表查询
     * @param hitachiRfxPrintQueryDTO
     * @param pageRequest
     * @return
     */
    Page<HitachiRfxPrintDTO> rfxPrintListQuery(HitachiRfxPrintQueryDTO hitachiRfxPrintQueryDTO, PageRequest pageRequest);

    /**
     * 询价单打印打包下载-日立物流
     * @param tenantId
     * @param hitachiSelectedPrintDTO
     */
    void postRfxPrintPackDownload(HttpServletRequest request, HttpServletResponse response, Long tenantId, HitachiSelectedPrintDTO hitachiSelectedPrintDTO);

    /**
     * 询价单打印-校验
     * @param tenantId
     * @param hitachiRfxPrintDTOList
     * @return
     */
    List<HitachiRfxConsigneeDTO> hitachiRfxPrintValidate(Long tenantId, List<HitachiRfxPrintDTO> hitachiRfxPrintDTOList);
}
