package org.srm.source.rfx.api.dto;

/**
 * srm 业务实体
 *
 * @author le.zhao@going-link.com 2021-3-28 12:58:37
 */
public class HitachiOperationUnitDTO extends OperationUnitDTO {

    private String companyCode;

    private Long sourceHeaderId;

    private Long quotationLineId;

    private Long organizationId;

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public Long getSourceHeaderId() {
        return sourceHeaderId;
    }

    public void setSourceHeaderId(Long sourceHeaderId) {
        this.sourceHeaderId = sourceHeaderId;
    }

    public Long getQuotationLineId() {
        return quotationLineId;
    }

    public void setQuotationLineId(Long quotationLineId) {
        this.quotationLineId = quotationLineId;
    }

    public Long getOrganizationId() { return organizationId; }

    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }
}
