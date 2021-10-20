package org.srm.source.rfx.api.dto;

import io.swagger.annotations.ApiModelProperty;

/**
 * @author guotao.yu@hand-china.com 2021/3/23 下午8:51
 */
public class HitachiRfxPrintQueryDTO {
    @ApiModelProperty(value = "租户id")
    private Long tenantId;
    @ApiModelProperty(value = "询价单单号")
    private String rfxNum;
    @ApiModelProperty(value = "个人")
    private Integer personalFlag;
    @ApiModelProperty(value = "自己所属部门")
    private Integer ownDepartmentFlag;
    @ApiModelProperty(value = "供应商编码")
    private String supplierCompanyCode;
    @ApiModelProperty(value = "供应商名称")
    private String supplierCompanyName;
    @ApiModelProperty(value = "物料名称")
    private String itemName;
    @ApiModelProperty(value = "是否已打印完成")
    private Integer printCompletedFlag;

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getRfxNum() {
        return rfxNum;
    }

    public void setRfxNum(String rfxNum) {
        this.rfxNum = rfxNum;
    }

    public Integer getPersonalFlag() {
        return personalFlag;
    }

    public void setPersonalFlag(Integer personalFlag) {
        this.personalFlag = personalFlag;
    }

    public Integer getOwnDepartmentFlag() {
        return ownDepartmentFlag;
    }

    public void setOwnDepartmentFlag(Integer ownDepartmentFlag) {
        this.ownDepartmentFlag = ownDepartmentFlag;
    }

    public String getSupplierCompanyCode() {
        return supplierCompanyCode;
    }

    public void setSupplierCompanyCode(String supplierCompanyCode) {
        this.supplierCompanyCode = supplierCompanyCode;
    }

    public String getSupplierCompanyName() {
        return supplierCompanyName;
    }

    public void setSupplierCompanyName(String supplierCompanyName) {
        this.supplierCompanyName = supplierCompanyName;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Integer getPrintCompletedFlag() {
        return printCompletedFlag;
    }

    public void setPrintCompletedFlag(Integer printCompletedFlag) {
        this.printCompletedFlag = printCompletedFlag;
    }
}
