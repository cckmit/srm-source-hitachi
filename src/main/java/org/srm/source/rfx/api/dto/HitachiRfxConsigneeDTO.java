package org.srm.source.rfx.api.dto;

/**
 * <p>
 * 收货方信息
 * </p>
 *
 * @author junheng.duan@going-link.com 2021/06/01 13:39
 */
public class HitachiRfxConsigneeDTO {
    /**
     * 负责人职位
     */
    private String position;
    /**
     * 负责人名称
     */
    private String applicant;
    /**
     * 目标地址
     */
    private String deliveryAddress;
    /**
     * 公司代码
     */
    private String companyCode;
    /**
     * 总部代码
     */
    private String department;
    /**
     * 所课
     */
    private String subDepartment;
    /**
     * 所课名
     */
    private String subDeptName;
    /**
     * SAP供应商编码
     */
    private String supplier;
    /**
     * SRM供应商名称
     */
    private String srmSupplier;
    /**
     * 序列号
     */
    private String no;

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

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getSubDepartment() {
        return subDepartment;
    }

    public void setSubDepartment(String subDepartment) {
        this.subDepartment = subDepartment;
    }

    public String getSubDeptName() {
        return subDeptName;
    }

    public void setSubDeptName(String subDeptName) {
        this.subDeptName = subDeptName;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getSrmSupplier() {
        return srmSupplier;
    }

    public void setSrmSupplier(String srmSupplier) {
        this.srmSupplier = srmSupplier;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    @Override
    public String toString() {
        return "HitachiRfxConsigneeDTO{" +
                "position='" + position + '\'' +
                ", applicant='" + applicant + '\'' +
                ", deliveryAddress='" + deliveryAddress + '\'' +
                ", companyCode='" + companyCode + '\'' +
                ", department='" + department + '\'' +
                ", subDepartment='" + subDepartment + '\'' +
                ", supplier='" + supplier + '\'' +
                ", srmSupplier='" + srmSupplier + '\'' +
                ", no='" + no + '\'' +
                '}';
    }

    public void initPrintData() {
        if (this.applicant == null) {
            this.applicant = "";
        }
        if (this.position == null) {
            this.position = "";
        }
        if (this.subDepartment == null) {
            this.subDepartment = "";
        }
    }
}
