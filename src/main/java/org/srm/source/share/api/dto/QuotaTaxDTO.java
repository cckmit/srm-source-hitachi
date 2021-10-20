package org.srm.source.share.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * <p>
 * 定额税
 * </p>
 *
 * @author zili.wang01@hand-china.com
 * @date 2021/03/25 10:29
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class QuotaTaxDTO {
    public static final String FIELD_LIGHT_OIL_TAX_PRICE_FLAG = "lightOilTaxPriceFlag";
    public static final String FIELD_LIGHT_OIL_TAX_FLAG = "lightOilTaxFlag";

    /**
     * 业务ID
     */
    private Long id;

    /**
     * 基准价 1/0
     */
    private Integer benchmarkPriceFlag;

    /**
     * 单价
     */
    private BigDecimal unitPrice;

    /**
     * 数量
     */
    private BigDecimal quantity;

    /**
     * 单位id id与code二选一 code优先
     */
    private Long uomId;

    /**
     * 单位code
     */
    private String uomCode;

    /**
     * 税率id id与code二选一 code优先
     */
    private Long taxId;

    /**
     * 税率code
     */
    private String taxCode;

    /**
     * 币种id id与code二选一 code优先
     */
    private Long currencyId;

    /**
     * 币种code
     */
    private String currencyCode;

    /**
     * 消费税额
     */
    private BigDecimal amount;

    /**
     * 物料id
     */
    private Long itemId;

    /**
     * 品类id
     */
    private Long categoryId;


    /**
     * 总账科目
     */
    private Long accountSubjectId;


    // 返回字段
    // ------------------------------------------------------------------------------

    /**
     * 是否含税
     */
    private Integer includedTaxFlag;

    /**
     * 是否含轻油税单价
     */
    private Integer lightOilTaxPriceFlag;

    /**
     * 是否有轻油税
     */
    private Integer lightOilTaxFlag;

    /**
     * 不含轻油税含消费税单价
     */
    private BigDecimal includeConsumeTaxUnitPrice;

    /**
     * 不含轻油税含消费含税金额
     */
    private BigDecimal includeConsumeTaxAmount;

    /**
     * 含轻油税含消费税金额=含税金额
     */
    private BigDecimal includeTaxAmount;

    /**
     * 消费税金额
     */
    private BigDecimal consumeTaxAmount;

    /**
     * 不含轻油税不含消费税金额
     */
    private BigDecimal excludeTaxAmount;

    /**
     * 含轻油税不含消费税金额
     */
    private BigDecimal includeQuotaTaxAmount;

    /**
     * 不含轻油税不含消费税单价=不含税单价
     */
    private BigDecimal excludeTaxUnitPrice;

    /**
     * 含轻油税不含消费税单价
     */
    private BigDecimal excludeConsumeTaxUnitPrice;

    /**
     * 含轻油税含消费税单价
     */
    private BigDecimal includeTaxUnitPrice;

    /**
     * 轻油税金额
     */
    private BigDecimal quotaTaxAmount;

    public Integer getLightOilTaxPriceFlag() {
        return lightOilTaxPriceFlag;
    }

    public void setLightOilTaxPriceFlag(Integer lightOilTaxPriceFlag) {
        this.lightOilTaxPriceFlag = lightOilTaxPriceFlag;
    }

    public Integer getLightOilTaxFlag() {
        return lightOilTaxFlag;
    }

    public void setLightOilTaxFlag(Integer lightOilTaxFlag) {
        this.lightOilTaxFlag = lightOilTaxFlag;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }


    public String getUomCode() {
        return uomCode;
    }

    public void setUomCode(String uomCode) {
        this.uomCode = uomCode;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getBenchmarkPriceFlag() {
        return benchmarkPriceFlag;
    }

    public void setBenchmarkPriceFlag(Integer benchmarkPriceFlag) {
        this.benchmarkPriceFlag = benchmarkPriceFlag;
    }

    public BigDecimal getIncludeConsumeTaxUnitPrice() {
        return includeConsumeTaxUnitPrice;
    }

    public void setIncludeConsumeTaxUnitPrice(BigDecimal includeConsumeTaxUnitPrice) {
        this.includeConsumeTaxUnitPrice = includeConsumeTaxUnitPrice;
    }

    public BigDecimal getIncludeConsumeTaxAmount() {
        return includeConsumeTaxAmount;
    }

    public void setIncludeConsumeTaxAmount(BigDecimal includeConsumeTaxAmount) {
        this.includeConsumeTaxAmount = includeConsumeTaxAmount;
    }

    public BigDecimal getIncludeTaxAmount() {
        return includeTaxAmount;
    }

    public void setIncludeTaxAmount(BigDecimal includeTaxAmount) {
        this.includeTaxAmount = includeTaxAmount;
    }

    public BigDecimal getConsumeTaxAmount() {
        return consumeTaxAmount;
    }

    public void setConsumeTaxAmount(BigDecimal consumeTaxAmount) {
        this.consumeTaxAmount = consumeTaxAmount;
    }

    public BigDecimal getExcludeTaxAmount() {
        return excludeTaxAmount;
    }

    public void setExcludeTaxAmount(BigDecimal excludeTaxAmount) {
        this.excludeTaxAmount = excludeTaxAmount;
    }

    public BigDecimal getIncludeQuotaTaxAmount() {
        return includeQuotaTaxAmount;
    }

    public void setIncludeQuotaTaxAmount(BigDecimal includeQuotaTaxAmount) {
        this.includeQuotaTaxAmount = includeQuotaTaxAmount;
    }

    public BigDecimal getExcludeTaxUnitPrice() {
        return excludeTaxUnitPrice;
    }

    public void setExcludeTaxUnitPrice(BigDecimal excludeTaxUnitPrice) {
        this.excludeTaxUnitPrice = excludeTaxUnitPrice;
    }

    public BigDecimal getExcludeConsumeTaxUnitPrice() {
        return excludeConsumeTaxUnitPrice;
    }

    public void setExcludeConsumeTaxUnitPrice(BigDecimal excludeConsumeTaxUnitPrice) {
        this.excludeConsumeTaxUnitPrice = excludeConsumeTaxUnitPrice;
    }

    public BigDecimal getIncludeTaxUnitPrice() {
        return includeTaxUnitPrice;
    }

    public void setIncludeTaxUnitPrice(BigDecimal includeTaxUnitPrice) {
        this.includeTaxUnitPrice = includeTaxUnitPrice;
    }

    public Long getUomId() {
        return uomId;
    }

    public void setUomId(Long uomId) {
        this.uomId = uomId;
    }

    public Long getTaxId() {
        return taxId;
    }

    public void setTaxId(Long taxId) {
        this.taxId = taxId;
    }

    public Long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Long currencyId) {
        this.currencyId = currencyId;
    }

    public Long getItemId() {
        return itemId;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getIncludedTaxFlag() {
        return includedTaxFlag;
    }

    public void setIncludedTaxFlag(Integer includedTaxFlag) {
        this.includedTaxFlag = includedTaxFlag;
    }

    public BigDecimal getQuotaTaxAmount() {
        return quotaTaxAmount;
    }

    public void setQuotaTaxAmount(BigDecimal quotaTaxAmount) {
        this.quotaTaxAmount = quotaTaxAmount;
    }

    public Long getAccountSubjectId() {
        return accountSubjectId;
    }

    public void setAccountSubjectId(Long accountSubjectId) {
        this.accountSubjectId = accountSubjectId;
    }
}
