package org.srm.source.rfx.api.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author tian.yu@going-link.com 2021/6/28 17:19
 * 日立物流商场主地址配置表DTO
 */
public class HitachiMallAddressMainDTO {
    @ApiModelProperty("主键")
    private Long id;

    @ApiModelProperty("租户id")
    private Long tenantId;

    @ApiModelProperty("地址id")
    private Long addressId;

    @ApiModelProperty("地址类型")
    private String addressType;

    @ApiModelProperty("公司id")
    private Long companyId;

    @ApiModelProperty("是否主地址")
    private String isMainAddress;

    @ApiModelProperty("组织id")
    private Long invOrganizationId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(Long addressId) {
        this.addressId = addressId;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getIsMainAddress() {
        return isMainAddress;
    }

    public void setIsMainAddress(String isMainAddress) {
        this.isMainAddress = isMainAddress;
    }

    public Long getInvOrganizationId() {
        return invOrganizationId;
    }

    public void setInvOrganizationId(Long invOrganizationId) {
        this.invOrganizationId = invOrganizationId;
    }

    @Override
    public String toString() {
        return "HitachiMallAddressMainDTO{" +
                "id=" + id +
                ", tenantId=" + tenantId +
                ", addressId=" + addressId +
                ", addressType='" + addressType + '\'' +
                ", companyId=" + companyId +
                ", isMainAddress=" + isMainAddress +
                ", invOrganizationId=" + invOrganizationId +
                '}';
    }
}
