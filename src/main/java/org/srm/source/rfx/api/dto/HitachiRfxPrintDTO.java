package org.srm.source.rfx.api.dto;

import java.math.BigDecimal;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.boot.platform.lov.annotation.LovValue;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author guotao.yu@hand-china.com 2021/3/23 下午8:51
 */
public class HitachiRfxPrintDTO {
    @ApiModelProperty(value = "唯一key，供前端使用")
    private String uniqueKey;
    @ApiModelProperty(value = "询价单id")
    @Encrypt
    private Long rfxHeaderId;
    @ApiModelProperty(value = "询价单单号")
    private String rfxNum;
    @ApiModelProperty(value = "已打印过一次标识")
    private Integer attributeTinyint1;
    @ApiModelProperty(value = "第一次打印时间")
    private Date attributeDatetime1;
    @ApiModelProperty(value = "物料行ID")
    @Encrypt
    private Long rfxLineItemId;
    @ApiModelProperty(value = "物料名称")
    private String itemName;
    @ApiModelProperty(value = "数量")
    private BigDecimal rfxQuantity;
    @ApiModelProperty(value = "单位")
    private String uomName;
    @ApiModelProperty(value = "关联法规")
    @LovValue(value = "SSR_RELATED_REGULATIONS", meaningField = "attributeVarchar14Meaning")
    private String attributeVarchar14;
    private String attributeVarchar14Meaning;
    @ApiModelProperty(value = "创建人部门")
    private String createdByUnitName;
    @ApiModelProperty(value = "创建人id")
    private Long createdBy;
    @ApiModelProperty(value = "创建人名称")
    private String createdByRealName;
    @ApiModelProperty(value = "供应商编码")
    private String supplierCompanyCode;
    @ApiModelProperty(value = "供应商名称")
    private String supplierCompanyName;
    @ApiModelProperty(value = "业务实体所属部门-所課名")
    private String divisionName;
    @ApiModelProperty(value = "业务实体所属部门编码-所課コード")
    private String divisionCode;
    @ApiModelProperty(value = "创建人所属公司编码")
    private String companyCode;
    @ApiModelProperty(value = "SAP供应商编码")
    private String sapSupplierCode;
    @ApiModelProperty(value = "注文書の送付先")
    private String deliveryAddress;
    @ApiModelProperty(value = "注文書の担当者の役職")
    private String position;
    @ApiModelProperty(value = "注文書の担当者")
    private String applicant;

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public Long getRfxLineItemId() {
        return rfxLineItemId;
    }

    public void setRfxLineItemId(Long rfxLineItemId) {
        this.rfxLineItemId = rfxLineItemId;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getRfxHeaderId() {
        return rfxHeaderId;
    }

    public void setRfxHeaderId(Long rfxHeaderId) {
        this.rfxHeaderId = rfxHeaderId;
    }

    public String getRfxNum() {
        return rfxNum;
    }

    public void setRfxNum(String rfxNum) {
        this.rfxNum = rfxNum;
    }

    public Integer getAttributeTinyint1() {
        return attributeTinyint1;
    }

    public void setAttributeTinyint1(Integer attributeTinyint1) {
        this.attributeTinyint1 = attributeTinyint1;
    }

    public Date getAttributeDatetime1() {
        return attributeDatetime1;
    }

    public void setAttributeDatetime1(Date attributeDatetime1) {
        this.attributeDatetime1 = attributeDatetime1;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getRfxQuantity() {
        return rfxQuantity;
    }

    public void setRfxQuantity(BigDecimal rfxQuantity) {
        this.rfxQuantity = rfxQuantity;
    }

    public String getUomName() {
        return uomName;
    }

    public void setUomName(String uomName) {
        this.uomName = uomName;
    }

    public String getAttributeVarchar14() {
        return attributeVarchar14;
    }

    public void setAttributeVarchar14(String attributeVarchar14) {
        this.attributeVarchar14 = attributeVarchar14;
    }

    public String getAttributeVarchar14Meaning() {
        return attributeVarchar14Meaning;
    }

    public void setAttributeVarchar14Meaning(String attributeVarchar14Meaning) {
        this.attributeVarchar14Meaning = attributeVarchar14Meaning;
    }

    public String getCreatedByUnitName() {
        return createdByUnitName;
    }

    public void setCreatedByUnitName(String createdByUnitName) {
        this.createdByUnitName = createdByUnitName;
    }

    public String getCreatedByRealName() {
        return createdByRealName;
    }

    public void setCreatedByRealName(String createdByRealName) {
        this.createdByRealName = createdByRealName;
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

    public String getDivisionName() {
        return divisionName;
    }

    public void setDivisionName(String divisionName) {
        this.divisionName = divisionName;
    }

    public String getDivisionCode() {
        return divisionCode;
    }

    public void setDivisionCode(String divisionCode) {
        this.divisionCode = divisionCode;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getSapSupplierCode() {
        return sapSupplierCode;
    }

    public void setSapSupplierCode(String sapSupplierCode) {
        this.sapSupplierCode = sapSupplierCode;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getApplicant() {
        return applicant;
    }

    public void setApplicant(String applicant) {
        this.applicant = applicant;
    }

    @Override
    public String toString() {
        return "HitachiRfxPrintDTO{" +
                "uniqueKey='" + uniqueKey + '\'' +
                ", rfxHeaderId=" + rfxHeaderId +
                ", rfxNum='" + rfxNum + '\'' +
                ", attributeTinyint1=" + attributeTinyint1 +
                ", attributeDatetime1=" + attributeDatetime1 +
                ", rfxLineItemId=" + rfxLineItemId +
                ", itemName='" + itemName + '\'' +
                ", rfxQuantity=" + rfxQuantity +
                ", uomName='" + uomName + '\'' +
                ", attributeVarchar14='" + attributeVarchar14 + '\'' +
                ", attributeVarchar14Meaning='" + attributeVarchar14Meaning + '\'' +
                ", createdByUnitName='" + createdByUnitName + '\'' +
                ", createdBy=" + createdBy +
                ", createdByRealName='" + createdByRealName + '\'' +
                ", supplierCompanyCode='" + supplierCompanyCode + '\'' +
                ", supplierCompanyName='" + supplierCompanyName + '\'' +
                ", divisionName='" + divisionName + '\'' +
                ", divisionCode='" + divisionCode + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", sapSupplierCode='" + sapSupplierCode + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", position='" + position + '\'' +
                ", applicant='" + applicant + '\'' +
                '}';
    }
}
