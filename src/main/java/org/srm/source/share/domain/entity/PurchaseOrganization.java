package org.srm.source.share.domain.entity;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.validation.constraints.NotBlank;

import io.choerodon.mybatis.domain.AuditDomain;
import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 采购组织
 *
 * @author keling.liu@hand-china.com 2021-05-03 19:44:48
 */
@ApiModel("采购组织")
@VersionAudit
@ModifyAudit
@JsonInclude(value = JsonInclude.Include.NON_NULL)
@Table(name = "hpfm_purchase_organization")
public class PurchaseOrganization extends AuditDomain {

    public static final String FIELD_PURCHASE_ORG_ID = "purchaseOrgId";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_ORGANIZATION_CODE = "organizationCode";
    public static final String FIELD_ORGANIZATION_NAME = "organizationName";
    public static final String FIELD_SOURCE_CODE = "sourceCode";
    public static final String FIELD_ENABLED_FLAG = "enabledFlag";
    public static final String FIELD_EXTERNAL_SYSTEM_CODE = "externalSystemCode";
    public static final String FIELD_ATTRIBUTE1 = "attribute1";
    public static final String FIELD_ATTRIBUTE2 = "attribute2";
    public static final String FIELD_ATTRIBUTE3 = "attribute3";
    public static final String FIELD_ATTRIBUTE4 = "attribute4";
    public static final String FIELD_ATTRIBUTE5 = "attribute5";
    public static final String FIELD_ATTRIBUTE6 = "attribute6";
    public static final String FIELD_ATTRIBUTE7 = "attribute7";
    public static final String FIELD_ATTRIBUTE8 = "attribute8";
    public static final String FIELD_ATTRIBUTE9 = "attribute9";
    public static final String FIELD_ATTRIBUTE10 = "attribute10";
    public static final String FIELD_ATTRIBUTE11 = "attribute11";
    public static final String FIELD_ATTRIBUTE12 = "attribute12";
    public static final String FIELD_ATTRIBUTE13 = "attribute13";
    public static final String FIELD_ATTRIBUTE14 = "attribute14";
    public static final String FIELD_ATTRIBUTE15 = "attribute15";

//
// 业务方法(按public protected private顺序排列)
// ------------------------------------------------------------------------------

//
// 数据库字段
// ------------------------------------------------------------------------------


    @ApiModelProperty("")
    @Id
    @GeneratedValue
    private Long purchaseOrgId;
    @ApiModelProperty(value = "租户ID，hpfm_tenant.tenant_id", required = true)
    @NotNull
    private Long tenantId;
    @ApiModelProperty(value = "采购组织编码", required = true)
    @NotBlank
    private String organizationCode;
    @ApiModelProperty(value = "采购组织名称", required = true)
    @NotBlank
    private String organizationName;
    @ApiModelProperty(value = "数据来源，值集：HPFM.DATA_SOURCE", required = true)
    @NotBlank
    private String sourceCode;
    @ApiModelProperty(value = "是否启用。1启用，0未启用", required = true)
    @NotNull
    private Integer enabledFlag;
    @ApiModelProperty(value = "外部来源系统代码")
    private String externalSystemCode;
    @ApiModelProperty(value = "")
    private String attribute1;
    @ApiModelProperty(value = "")
    private String attribute2;
    @ApiModelProperty(value = "")
    private String attribute3;
    @ApiModelProperty(value = "")
    private String attribute4;
    @ApiModelProperty(value = "")
    private String attribute5;
    @ApiModelProperty(value = "")
    private String attribute6;
    @ApiModelProperty(value = "")
    private String attribute7;
    @ApiModelProperty(value = "")
    private String attribute8;
    @ApiModelProperty(value = "")
    private String attribute9;
    @ApiModelProperty(value = "")
    private String attribute10;
    @ApiModelProperty(value = "")
    private String attribute11;
    @ApiModelProperty(value = "")
    private String attribute12;
    @ApiModelProperty(value = "")
    private String attribute13;
    @ApiModelProperty(value = "")
    private String attribute14;
    @ApiModelProperty(value = "")
    private String attribute15;

//
// 非数据库字段
// ------------------------------------------------------------------------------

//
// getter/setter
// ------------------------------------------------------------------------------

    /**
     * @return
     */
    public Long getPurchaseOrgId() {
        return purchaseOrgId;
    }

    public PurchaseOrganization setPurchaseOrgId(Long purchaseOrgId) {
        this.purchaseOrgId = purchaseOrgId;
        return this;
    }

    /**
     * @return 租户ID，hpfm_tenant.tenant_id
     */
    public Long getTenantId() {
        return tenantId;
    }

    public PurchaseOrganization setTenantId(Long tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    /**
     * @return 采购组织编码
     */
    public String getOrganizationCode() {
        return organizationCode;
    }

    public PurchaseOrganization setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
        return this;
    }

    /**
     * @return 采购组织名称
     */
    public String getOrganizationName() {
        return organizationName;
    }

    public PurchaseOrganization setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
        return this;
    }

    /**
     * @return 数据来源，值集：HPFM.DATA_SOURCE
     */
    public String getSourceCode() {
        return sourceCode;
    }

    public PurchaseOrganization setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
        return this;
    }

    /**
     * @return 是否启用。1启用，0未启用
     */
    public Integer getEnabledFlag() {
        return enabledFlag;
    }

    public PurchaseOrganization setEnabledFlag(Integer enabledFlag) {
        this.enabledFlag = enabledFlag;
        return this;
    }

    /**
     * @return 外部来源系统代码
     */
    public String getExternalSystemCode() {
        return externalSystemCode;
    }

    public PurchaseOrganization setExternalSystemCode(String externalSystemCode) {
        this.externalSystemCode = externalSystemCode;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute1() {
        return attribute1;
    }

    public PurchaseOrganization setAttribute1(String attribute1) {
        this.attribute1 = attribute1;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute2() {
        return attribute2;
    }

    public PurchaseOrganization setAttribute2(String attribute2) {
        this.attribute2 = attribute2;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute3() {
        return attribute3;
    }

    public PurchaseOrganization setAttribute3(String attribute3) {
        this.attribute3 = attribute3;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute4() {
        return attribute4;
    }

    public PurchaseOrganization setAttribute4(String attribute4) {
        this.attribute4 = attribute4;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute5() {
        return attribute5;
    }

    public PurchaseOrganization setAttribute5(String attribute5) {
        this.attribute5 = attribute5;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute6() {
        return attribute6;
    }

    public PurchaseOrganization setAttribute6(String attribute6) {
        this.attribute6 = attribute6;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute7() {
        return attribute7;
    }

    public PurchaseOrganization setAttribute7(String attribute7) {
        this.attribute7 = attribute7;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute8() {
        return attribute8;
    }

    public PurchaseOrganization setAttribute8(String attribute8) {
        this.attribute8 = attribute8;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute9() {
        return attribute9;
    }

    public PurchaseOrganization setAttribute9(String attribute9) {
        this.attribute9 = attribute9;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute10() {
        return attribute10;
    }

    public PurchaseOrganization setAttribute10(String attribute10) {
        this.attribute10 = attribute10;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute11() {
        return attribute11;
    }

    public PurchaseOrganization setAttribute11(String attribute11) {
        this.attribute11 = attribute11;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute12() {
        return attribute12;
    }

    public PurchaseOrganization setAttribute12(String attribute12) {
        this.attribute12 = attribute12;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute13() {
        return attribute13;
    }

    public PurchaseOrganization setAttribute13(String attribute13) {
        this.attribute13 = attribute13;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute14() {
        return attribute14;
    }

    public PurchaseOrganization setAttribute14(String attribute14) {
        this.attribute14 = attribute14;
        return this;
    }

    /**
     * @return
     */
    public String getAttribute15() {
        return attribute15;
    }

    public PurchaseOrganization setAttribute15(String attribute15) {
        this.attribute15 = attribute15;
        return this;
    }
}
