package org.srm.source.share.domain.entity;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.NotBlank;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/**
 * 价格适用范围头表
 *
 * @author wei.yi@hand-china.com 2020-07-14 15:40:28
 */
@ApiModel("价格适用范围头表")
@VersionAudit
@ModifyAudit
@Table(name = "ssrc_price_app_scope")
public class PriceAppScope extends AuditDomain {

    public static final String FIELD_APP_SCOPE_ID = "appScopeId";
    public static final String FIELD_PRICE_LIB_ID = "priceLibId";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_DIMENSION_CODE = "dimensionCode";
    public static final String FIELD_INCLUDE_ALL_FLAG = "includeAllFlag";
	public static final String FIELD_PRICE_APP_SCOPE_LINES = "priceAppScopeLines";

	public PriceAppScope(){}

	public PriceAppScope(Long tenantId , Long priceLibId){
		this.tenantId = tenantId;
		this.priceLibId = priceLibId;
	}


	public PriceAppScope(Long tenantId,Long priceLibId,String dimensionCode ){
		this(tenantId,priceLibId);
		this.dimensionCode = dimensionCode;
	}

	public PriceAppScope(Long tenantId,Long priceLibId,List<String> dimensionCodeList ){
		this(tenantId,priceLibId);
		this.dimensionCodeList = dimensionCodeList;
	}

	public PriceAppScope(Long tenantId,Long priceLibId,String dimensionCode ,String dimensionName){
		this(tenantId,priceLibId,dimensionCode);
		this.dimensionName = dimensionName;
	}

	public PriceAppScope(Long tenantId,Long priceLibId,String dimensionCode ,String dimensionName,Integer includeAllFlag){
		this(tenantId,priceLibId,dimensionCode,dimensionName);
		this.includeAllFlag = includeAllFlag;
	}

	public PriceAppScope(Long tenantId, String dimensionCode, Integer includeAllFlag, List<PriceAppScopeLine> priceAppScopeLines) {
		this.tenantId = tenantId;
		this.dimensionCode = dimensionCode;
		this.includeAllFlag = includeAllFlag;
		this.priceAppScopeLines = priceAppScopeLines;
	}

	@ApiModelProperty("表ID，主键，供其他表做外键")
    @Id
    @GeneratedValue
	@Encrypt
    private Long appScopeId;
    @ApiModelProperty(value = "价格库表",required = true)
    @NotNull
    private Long priceLibId;
    @ApiModelProperty(value = "租户id",required = true)
    @NotNull
    private Long tenantId;
    @ApiModelProperty(value = "维度编码",required = true)
    @NotBlank
    private String dimensionCode;
	@ApiModelProperty(value = "维度编码",required = true)
	@Transient
	private List<String> dimensionCodeList;
    @ApiModelProperty(value = "是否包含所有标识",required = true)
    @NotNull
    private Integer includeAllFlag;

    @ApiModelProperty
	@Transient
	private List<PriceAppScopeLine> priceAppScopeLines;

	@ApiModelProperty(value = "维度名称")
	@Transient
	private String dimensionName;
	@Transient
	private String fieldWidget;
	@Transient
	private String sourceCode;


	//
    // 非数据库字段
    // ------------------------------------------------------------------------------


	public List<String> getDimensionCodeList() {
		return dimensionCodeList;
	}

	public void setDimensionCodeList(List<String> dimensionCodeList) {
		this.dimensionCodeList = dimensionCodeList;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

	public String getDimensionName() {
		return dimensionName;
	}

	public void setDimensionName(String dimensionName) {
		this.dimensionName = dimensionName;
	}

	public String getFieldWidget() {
		return fieldWidget;
	}

	public void setFieldWidget(String fieldWidget) {
		this.fieldWidget = fieldWidget;
	}

	//
    // getter/setter
    // ------------------------------------------------------------------------------

    /**
     * @return 表ID，主键，供其他表做外键
     */
	public Long getAppScopeId() {
		return appScopeId;
	}

	public void setAppScopeId(Long appScopeId) {
		this.appScopeId = appScopeId;
	}
    /**
     * @return 价格库表
     */
	public Long getPriceLibId() {
		return priceLibId;
	}

	public void setPriceLibId(Long priceLibId) {
		this.priceLibId = priceLibId;
	}
    /**
     * @return 租户id
     */
	public Long getTenantId() {
		return tenantId;
	}

	public void setTenantId(Long tenantId) {
		this.tenantId = tenantId;
	}
    /**
     * @return 维度编码
     */
	public String getDimensionCode() {
		return dimensionCode;
	}

	public void setDimensionCode(String dimensionCode) {
		this.dimensionCode = dimensionCode;
	}
    /**
     * @return 是否包含所有标识
     */
	public Integer getIncludeAllFlag() {
		return includeAllFlag;
	}

	public void setIncludeAllFlag(Integer includeAllFlag) {
		this.includeAllFlag = includeAllFlag;
	}

	public List<PriceAppScopeLine> getPriceAppScopeLines() {
		return priceAppScopeLines;
	}

	public void setPriceAppScopeLines(List<PriceAppScopeLine> priceAppScopeLines) {
		this.priceAppScopeLines = priceAppScopeLines;
	}

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        PriceAppScope priceAppScope = (PriceAppScope) obj;
        if (this == priceAppScope) {
            return true;
        } else {
            return  Objects.equals(this.priceLibId,priceAppScope.priceLibId) && Objects.equals(this.dimensionCode ,priceAppScope.dimensionCode);
        }
    }
    @Override
    public int hashCode() {
        return Objects.hash(priceLibId, dimensionCode);
    }


    // public static PriceLibExcelSheet createExcelSheet(){
    //     PriceLibExcelSheet priceLibExcelSheet=new PriceLibExcelSheet(PriceLibExcelSheet.BATCH_CREATE,PriceLibExcelSheet.SCOPE_PROMPT_KEY);
    //     List<PriceLibExcelColumn> priceLibExcelColumnList=new ArrayList<>();
    //     priceLibExcelColumnList.add(new PriceLibExcelColumn(PriceLibExcelSheet.SCOPE_PROMPT_KEY,PriceLibExcelSheet.LINE_BATCH, PriceLibConstants.PriceLibTmplDim.FieldWidget.INPUT, IndexedColors.YELLOW1.getIndex()));
    //     priceLibExcelColumnList.add(new PriceLibExcelColumn(PriceLibExcelSheet.SCOPE_PROMPT_KEY,PriceLibExcelSheet.DIMENSION_CODE,PriceLibConstants.PriceLibTmplDim.FieldWidget.INPUT,IndexedColors.YELLOW1.getIndex()));
    //     priceLibExcelColumnList.add(new PriceLibExcelColumn(PriceLibExcelSheet.SCOPE_PROMPT_KEY,PriceLibExcelSheet.DATA_CODE,PriceLibConstants.PriceLibTmplDim.FieldWidget.INPUT));
    //     priceLibExcelColumnList.add(new PriceLibExcelColumn(PriceLibExcelSheet.SCOPE_PROMPT_KEY,PriceLibExcelSheet.INCLUDE_ALL_FLAG,PriceLibConstants.PriceLibTmplDim.FieldWidget.SWITCH));
    //     priceLibExcelSheet.setPriceLibExcelColumnList(priceLibExcelColumnList);
    //     return priceLibExcelSheet;
    // }
}
