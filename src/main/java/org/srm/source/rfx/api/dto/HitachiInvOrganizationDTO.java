package org.srm.source.rfx.api.dto;

/**
 * srm 库存组织
 *
 * @author le.zhao@going-link.com 2021-3-28 12:58:37
 */
public class HitachiInvOrganizationDTO extends InvOrganizationDTO {

    private Integer ouCodeFlag;

    private Long companyId;

    private Long addressId;

    public Integer getOuCodeFlag() {
        return ouCodeFlag;
    }

    public void setOuCodeFlag(Integer ouCodeFlag) {
        this.ouCodeFlag = ouCodeFlag;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }
}
