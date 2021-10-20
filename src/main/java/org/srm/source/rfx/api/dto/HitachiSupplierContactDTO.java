package org.srm.source.rfx.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.srm.common.mybatis.domain.ExpandDomain;

import java.util.Date;

/**
 * <p>
 * 供应商联系方式
 * </p>
 *
 * @author junheng.duan@going-link.com 2021/05/26 14:46
 */
@ApiModel(value = "HitachiSupplierContactDTO",description = "供应商联系人")
public class HitachiSupplierContactDTO extends ExpandDomain {

    /**
     * 表ID，主键，供其他表做外键
     */
    @ApiModelProperty(value = "表ID，主键，供其他表做外键")
    private Long supplierContactId;

    /**
     * 采购方租户id
     */
    @ApiModelProperty(value = "采购方租户id")
    private Long tenantId;

    /**
     * sslm_supplier_basic.supplier_basic_id
     */
    @ApiModelProperty(value = "sslm_supplier_basic.supplier_basic_id")
    private Long supplierBasicId;

    /**
     * 姓名
     */
    @ApiModelProperty(value = "姓名")
    private String name;

    /**
     * 性别，1男，0女
     */
    @ApiModelProperty(value = "性别，1男，0女")
    private Boolean gender;

    /**
     * 邮箱
     */
    @ApiModelProperty(value = "邮箱")
    private String mail;

    /**
     * 手机号
     */
    @ApiModelProperty(value = "手机号")
    private String mobilephone;

    /**
     *
     */
    @ApiModelProperty(value = "")
    private String telephone;

    /**
     * 证件类型，值集HPFM.ID_TYPE
     */
    @ApiModelProperty(value = "证件类型，值集HPFM.ID_TYPE")
    private String idType;

    /**
     * 证件号码
     */
    @ApiModelProperty(value = "证件号码")
    private String idNum;

    /**
     * 部门
     */
    @ApiModelProperty(value = "部门")
    private String department;

    /**
     * 职位
     */
    @ApiModelProperty(value = "职位")
    private String position;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String description;

    /**
     * 默认联系人标识
     */
    @ApiModelProperty(value = "默认联系人标识")
    private Boolean defaultFlag;

    /**
     * 启用标识
     */
    @ApiModelProperty(value = "启用标识")
    private Boolean enabledFlag;

    /**
     * 国际电话区号。
     */
    @ApiModelProperty(value = "国际电话区号。")
    private String internationalTelCode;


    /**
     * 调查模板ID，SSLM.INVESTIGATE_TEMPLATE_ID
     */
    @ApiModelProperty(value = "调查模板ID，SSLM.INVESTIGATE_TEMPLATE_ID")
    private Long investigateTemplateId;

    /**
     * 关联企业认证注册联系人表信息spfm_company_contacts.company_contact_id
     */
    @ApiModelProperty(value = "关联企业认证注册联系人表信息spfm_company_contacts.company_contact_id")
    private Long companyContactId;

    /**
     * 联系人类型
     */
    @ApiModelProperty(value = "联系人类型")
    private String contactType;

    /**
     * 股东
     */
    @ApiModelProperty(value = "股东")
    private Boolean shareholderFlag;

    /**
     * 持股比例
     */
    @ApiModelProperty(value = "持股比例")
    private String shareholdingRatio;

    /**
     * 法人
     */
    @ApiModelProperty(value = "法人")
    private Boolean legalFlag;

    /**
     * 更新状态
     */
    @ApiModelProperty(value = "更新状态")
    private String updateFlag;

    /**
     * 最后更新人
     */
    @ApiModelProperty(value = "最后更新人")
    private Long lastUpdated;

    /**
     *
     */
    @ApiModelProperty(value = "")
    private Date updateDate;


    public Long getSupplierContactId() {
        return supplierContactId;
    }

    public void setSupplierContactId(Long supplierContactId) {
        this.supplierContactId = supplierContactId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public Long getSupplierBasicId() {
        return supplierBasicId;
    }

    public void setSupplierBasicId(Long supplierBasicId) {
        this.supplierBasicId = supplierBasicId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getGender() {
        return gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMobilephone() {
        return mobilephone;
    }

    public void setMobilephone(String mobilephone) {
        this.mobilephone = mobilephone;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNum() {
        return idNum;
    }

    public void setIdNum(String idNum) {
        this.idNum = idNum;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getDefaultFlag() {
        return defaultFlag;
    }

    public void setDefaultFlag(Boolean defaultFlag) {
        this.defaultFlag = defaultFlag;
    }

    public Boolean getEnabledFlag() {
        return enabledFlag;
    }

    public void setEnabledFlag(Boolean enabledFlag) {
        this.enabledFlag = enabledFlag;
    }

    public String getInternationalTelCode() {
        return internationalTelCode;
    }

    public void setInternationalTelCode(String internationalTelCode) {
        this.internationalTelCode = internationalTelCode;
    }

    public Long getInvestigateTemplateId() {
        return investigateTemplateId;
    }

    public void setInvestigateTemplateId(Long investigateTemplateId) {
        this.investigateTemplateId = investigateTemplateId;
    }

    public Long getCompanyContactId() {
        return companyContactId;
    }

    public void setCompanyContactId(Long companyContactId) {
        this.companyContactId = companyContactId;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public Boolean getShareholderFlag() {
        return shareholderFlag;
    }

    public void setShareholderFlag(Boolean shareholderFlag) {
        this.shareholderFlag = shareholderFlag;
    }

    public String getShareholdingRatio() {
        return shareholdingRatio;
    }

    public void setShareholdingRatio(String shareholdingRatio) {
        this.shareholdingRatio = shareholdingRatio;
    }

    public Boolean getLegalFlag() {
        return legalFlag;
    }

    public void setLegalFlag(Boolean legalFlag) {
        this.legalFlag = legalFlag;
    }

    public String getUpdateFlag() {
        return updateFlag;
    }

    public void setUpdateFlag(String updateFlag) {
        this.updateFlag = updateFlag;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }
}
