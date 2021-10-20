package org.srm.source.rfx.api.dto;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author guotao.yu@going-link.com 2021/7/14 下午4:04
 */
public class HitachiSelectedPrintDTO {
    @ApiModelProperty(value = "勾选需要打印的行")
    private List<HitachiRfxPrintDTO> hitachiRfxPrintDTOList;
    @ApiModelProperty(value = "勾选的对应的注文書の送付先对象")
    private HitachiRfxConsigneeDTO hitachiRfxConsigneeDTO;

    public List<HitachiRfxPrintDTO> getHitachiRfxPrintDTOList() {
        return hitachiRfxPrintDTOList;
    }

    public void setHitachiRfxPrintDTOList(List<HitachiRfxPrintDTO> hitachiRfxPrintDTOList) {
        this.hitachiRfxPrintDTOList = hitachiRfxPrintDTOList;
    }

    public HitachiRfxConsigneeDTO getHitachiRfxConsigneeDTO() {
        return hitachiRfxConsigneeDTO;
    }

    public void setHitachiRfxConsigneeDTO(HitachiRfxConsigneeDTO hitachiRfxConsigneeDTO) {
        this.hitachiRfxConsigneeDTO = hitachiRfxConsigneeDTO;
    }
}
