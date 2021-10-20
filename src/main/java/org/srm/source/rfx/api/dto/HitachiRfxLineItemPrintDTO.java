package org.srm.source.rfx.api.dto;

import java.math.BigDecimal;
import java.util.Date;

import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

/**
 * @author guotao.yu@hand-china.com 2021/3/24 下午4:24
 */
public class HitachiRfxLineItemPrintDTO {
    @ApiModelProperty(value = "询价单单号")
    private String rfxNum;
    @ApiModelProperty(value = "型号")
    private String model;
    @ApiModelProperty(value = "采购申请行")
    @Encrypt
    private Long prLineId;
    @ApiModelProperty(value = "物料名称")
    private String itemName;
    @ApiModelProperty(value = "当前日期")
    private Date currentDate;
    @ApiModelProperty(value = "询价单备注")
    private String rfxRemark;
    @ApiModelProperty(value = "收获地址")
    private String receiveAddress;
    @ApiModelProperty(value = "收获人")
    private String receiveContactName;
    @ApiModelProperty(value = "收获联系电话")
    private String invTelephone;
    @ApiModelProperty(value = "数量")
    private BigDecimal rfxQuantity;
    @ApiModelProperty(value = "单位")
    private String uomName;
    @ApiModelProperty(value = "需求日期")
    private Date demandDate;
    @ApiModelProperty(value = "登录人")
    private String loginRealName;
    @ApiModelProperty(value = "发布最终审批人")
    private String  releaseApprovedBy;

    public String getRfxNum() {
        return rfxNum;
    }

    public void setRfxNum(String rfxNum) {
        this.rfxNum = rfxNum;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public String getRfxRemark() {
        return rfxRemark;
    }

    public void setRfxRemark(String rfxRemark) {
        this.rfxRemark = rfxRemark;
    }

    public String getReceiveAddress() {
        return receiveAddress;
    }

    public void setReceiveAddress(String receiveAddress) {
        this.receiveAddress = receiveAddress;
    }

    public String getReceiveContactName() {
        return receiveContactName;
    }

    public void setReceiveContactName(String receiveContactName) {
        this.receiveContactName = receiveContactName;
    }

    public String getInvTelephone() {
        return invTelephone;
    }

    public void setInvTelephone(String invTelephone) {
        this.invTelephone = invTelephone;
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

    public Date getDemandDate() {
        return demandDate;
    }

    public void setDemandDate(Date demandDate) {
        this.demandDate = demandDate;
    }

    public String getLoginRealName() {
        return loginRealName;
    }

    public void setLoginRealName(String loginRealName) {
        this.loginRealName = loginRealName;
    }

    public String getReleaseApprovedBy() {
        return releaseApprovedBy;
    }

    public void setReleaseApprovedBy(String releaseApprovedBy) {
        this.releaseApprovedBy = releaseApprovedBy;
    }

    public Long getPrLineId() {
        return prLineId;
    }

    public void setPrLineId(Long prLineId) {
        this.prLineId = prLineId;
    }
}
