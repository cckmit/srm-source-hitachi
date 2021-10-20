package org.srm.source.share.domain.entity;

import io.choerodon.mybatis.annotation.ModifyAudit;
import io.choerodon.mybatis.annotation.VersionAudit;
import io.choerodon.mybatis.domain.AuditDomain;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hzero.starter.keyencrypt.core.Encrypt;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 价格适用范围行表
 *
 * @author wei.yi@hand-china.com 2020-07-14 15:40:28
 */
@ApiModel("价格适用范围行表")
@VersionAudit
@ModifyAudit
@Table(name = "ssrc_price_app_scope_line")
public class PriceAppScopeLine extends AuditDomain {

    public static final String FIELD_SCOPE_LINE_ID = "scopeLineId";
    public static final String FIELD_APP_SCOPE_ID = "appScopeId";
    public static final String FIELD_TENANT_ID = "tenantId";
    public static final String FIELD_DATA_ID = "dataId";
    public static final String FIELD_DATA_CODE = "dataCode";
    public static final String FIELD_DATA_NAME = "dataName";
    public static final String FIELD_ENABLED_FLAG = "enabledFlag";

    public PriceAppScopeLine(){}

	public PriceAppScopeLine(Long tenantId, Long appScopeId){
    	this.tenantId = tenantId ;
    	this.appScopeId = appScopeId;
	}

	public PriceAppScopeLine(Long tenantId, Long dataId, Integer enabledFlag) {
		this.tenantId = tenantId;
		this.dataId = dataId;
		this.enabledFlag = enabledFlag;
	}

	public PriceAppScopeLine(Long tenantId, Long appScopeId, Long dataId , String dataCode , String dataName){
		this(tenantId,appScopeId);
		this.dataId = dataId;
		this.dataCode = dataCode;
		this.dataName = dataName;
	}

	// public PriceAppScopeLine(Long tenantId, Long appScopeId,CompanyOuInvorgDTO companyOuInvorgDTO){
	// 	this(tenantId,appScopeId);
	// 	this.dataId = companyOuInvorgDTO.getDataId();
	// 	this.dataCode = companyOuInvorgDTO.getDataCode();
	// 	this.dataName = companyOuInvorgDTO.getDataName();
	// }
	//
	// public PriceAppScopeLine(CompanyOuInvorgDTO companyOuInvorgDTO){
    // 	this.scopeLineId = companyOuInvorgDTO.getId();
    // 	this.key = companyOuInvorgDTO.getKey();
    // 	this.parentKey = companyOuInvorgDTO.getParentKey();
    // 	this.dataId = companyOuInvorgDTO.getDataId();
    // 	this.dataCode = companyOuInvorgDTO.getDataCode();
    // 	this.dataName = companyOuInvorgDTO.getDataName();
    // 	this.enabledFlag = companyOuInvorgDTO.getEnabledFlag();
    // 	this.setObjectVersionNumber(companyOuInvorgDTO.getObjectVersionNumber());
	// }


    @ApiModelProperty("表ID，主键，供其他表做外键")
    @Id
    @GeneratedValue
	@Encrypt
    private Long scopeLineId;
    @ApiModelProperty(value = "价格适用范围头表",required = true)
    @NotNull
	@Encrypt
    private Long appScopeId;
    @ApiModelProperty(value = "租户id",required = true)
    @NotNull
    private Long tenantId;
   @ApiModelProperty(value = "数据id")    
    private Long dataId;
   @ApiModelProperty(value = "数据code")    
    private String dataCode;
   @ApiModelProperty(value = "数据名称")    
    private String dataName;
   @NotNull
   // @FieldCompareAnt(zh = "启用" , en = "enabledFlag")
   private Integer enabledFlag;

	@ApiModelProperty(value = "维度编码")
	@Transient
	private String dimensionCode;

	@ApiModelProperty(value = "维度编码")
	@Transient
	private String key;

	@ApiModelProperty(value = "维度编码")
	@Transient
	private String parentKey;

	@ApiModelProperty(value = "维度编码")
	@Transient
	private Long priceLibId;
	@Transient
	private List<String> invOrganizationIds;
	@Transient
	private List<String> companyIds;
	@Transient
	private List<String> ouIds;
	@Transient
	private List<Long> priceLibIds;

	//
    // 非数据库字段
    // ------------------------------------------------------------------------------


	public List<Long> getPriceLibIds() {
		return priceLibIds;
	}

	public void setPriceLibIds(List<Long> priceLibIds) {
		this.priceLibIds = priceLibIds;
	}

	public List<String> getInvOrganizationIds() {
		return invOrganizationIds;
	}

	public void setInvOrganizationIds(List<String> invOrganizationIds) {
		this.invOrganizationIds = invOrganizationIds;
	}

	public List<String> getCompanyIds() {
		return companyIds;
	}

	public void setCompanyIds(List<String> companyIds) {
		this.companyIds = companyIds;
	}

	public List<String> getOuIds() {
		return ouIds;
	}

	public void setOuIds(List<String> ouIds) {
		this.ouIds = ouIds;
	}

	public Long getPriceLibId() {
		return priceLibId;
	}

	public void setPriceLibId(Long priceLibId) {
		this.priceLibId = priceLibId;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getParentKey() {
		return parentKey;
	}

	public void setParentKey(String parentKey) {
		this.parentKey = parentKey;
	}

	public String getDimensionCode() {
		return dimensionCode;
	}

	public void setDimensionCode(String dimensionCode) {
		this.dimensionCode = dimensionCode;
	}


	//
    // getter/setter
    // ------------------------------------------------------------------------------


	public Integer getEnabledFlag() {
		return enabledFlag;
	}

	public void setEnabledFlag(Integer enabledFlag) {
		this.enabledFlag = enabledFlag;
	}

	/**
     * @return 表ID，主键，供其他表做外键
     */
	public Long getScopeLineId() {
		return scopeLineId;
	}

	public void setScopeLineId(Long scopeLineId) {
		this.scopeLineId = scopeLineId;
	}
    /**
     * @return 价格适用范围头表
     */
	public Long getAppScopeId() {
		return appScopeId;
	}

	public void setAppScopeId(Long appScopeId) {
		this.appScopeId = appScopeId;
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
     * @return 数据id
     */
	public Long getDataId() {
		return dataId;
	}

	public void setDataId(Long dataId) {
		this.dataId = dataId;
	}
    /**
     * @return 数据code
     */
	public String getDataCode() {
		return dataCode;
	}

	public void setDataCode(String dataCode) {
		this.dataCode = dataCode;
	}
    /**
     * @return 数据名称
     */
	public String getDataName() {
		return dataName;
	}

	public void setDataName(String dataName) {
		this.dataName = dataName;
	}


	// public static List<PriceAppScopeLine> toPriceAppScopeLineList(List<CompanyOuInvorgDTO> companyOuInvorgDTOList){
	// 	if(CollectionUtils.isEmpty(companyOuInvorgDTOList)){
	// 		return Collections.emptyList();
	// 	}
	// 	List<PriceAppScopeLine> result = new ArrayList<>();
	// 	companyOuInvorgDTOList.forEach(companyOuInvorgDTO -> {
	// 		result.add(new PriceAppScopeLine(companyOuInvorgDTO));
	// 	});
	// 	return result;
	// }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        PriceAppScopeLine priceAppScopeLine = (PriceAppScopeLine) obj;
        if (this == priceAppScopeLine) {
            return true;
        } else {
            return  Objects.equals(this.priceLibId,priceAppScopeLine.priceLibId) && Objects.equals(this.dataCode ,priceAppScopeLine.dataCode);
        }
    }
    @Override
    public int hashCode() {
        return Objects.hash(priceLibId, dataId);
    }


    public static Map beanToMap(PriceAppScope priceAppScope, PriceAppScopeLine priceAppScopeLine){
		Map map = new HashMap();
		map.put(PriceAppScope.FIELD_APP_SCOPE_ID,priceAppScope.getAppScopeId());
		map.put(PriceAppScope.FIELD_DIMENSION_CODE,priceAppScope.getDimensionCode());
		map.put(PriceAppScope.FIELD_INCLUDE_ALL_FLAG,priceAppScope.getIncludeAllFlag());
		map.put(PriceAppScopeLine.FIELD_SCOPE_LINE_ID,priceAppScopeLine == null ? null:priceAppScopeLine.getScopeLineId());
		map.put(PriceAppScopeLine.FIELD_ENABLED_FLAG,priceAppScopeLine == null ? null:priceAppScopeLine.getEnabledFlag());
		map.put(PriceAppScopeLine.FIELD_DATA_ID,priceAppScopeLine == null ? null:priceAppScopeLine.getDataId());
		map.put(PriceAppScopeLine.FIELD_DATA_CODE,priceAppScopeLine == null ? null:priceAppScopeLine.getDataCode());
		map.put(PriceAppScopeLine.FIELD_DATA_NAME,priceAppScopeLine == null ? null:priceAppScopeLine.getDataName());
		return map;
	}
}
