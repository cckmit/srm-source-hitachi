package org.srm.source.share.api.dto;

import java.util.List;

/**
 * <p>
 * 定额税返回dto
 * </p>
 *
 * @author zili.wang01@hand-china.com
 * @date 2021/03/25 16:12
 */
public class QuotaTaxReturnDTO {

    /**
     * 业务ID
     */
    private Long id;

    private Integer errorFlag;

    private String errorMessage;

    private List<QuotaTaxDTO> quotaTaxList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getErrorFlag() {
        return errorFlag;
    }

    public void setErrorFlag(Integer errorFlag) {
        this.errorFlag = errorFlag;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<QuotaTaxDTO> getQuotaTaxList() {
        return quotaTaxList;
    }

    public void setQuotaTaxList(List<QuotaTaxDTO> quotaTaxList) {
        this.quotaTaxList = quotaTaxList;
    }

    @Override
    public String toString() {
        return "QuotaTaxReturnDTO{" +
                "id=" + id +
                ", errorFlag=" + errorFlag +
                ", errorMessage='" + errorMessage + '\'' +
                ", quotaTaxList=" + quotaTaxList +
                '}';
    }
}
