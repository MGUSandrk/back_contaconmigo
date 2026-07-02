package com.sistema_contable.sistema.contable.dto.sales;

import java.util.Date;
import java.util.List;

import com.sistema_contable.sistema.contable.model.sales.InvoiceType;

public class InvoiceResponseDTO {

    private Long id;
    private String invoiceNumber;
    private InvoiceType invoiceType;
    private Date dateCreated;
    private String clientFullName;
    private String clientCuit;
    private String sellerFullName;
    private String entityName;
    private String entityCuit;
    private String entityCommercialAddress;
    private String entityGrossIncomeNumber;
    private String entityVatCondition;
    private Date entityActivityStartDate;
    private Integer salesPoint;
    private Double subtotal;
    private Double discountAmount;
    private Double total;
    private Integer installments;
    private List<InvoiceItemResponseDTO> items;
    private List<PaymentDetailResponseDTO> paymentDetails;
    private String costingMethod;
    private Double cmvAmount;
    private String legalInvoiceNumber;
    private String cae;
    private Date caeExpirationDate;
    private String qrCodeBase64;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public InvoiceType getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(InvoiceType invoiceType) {
        this.invoiceType = invoiceType;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getClientFullName() {
        return clientFullName;
    }

    public void setClientFullName(String clientFullName) {
        this.clientFullName = clientFullName;
    }

    public String getClientCuit() {
        return clientCuit;
    }

    public void setClientCuit(String clientCuit) {
        this.clientCuit = clientCuit;
    }

    public String getSellerFullName() {
        return sellerFullName;
    }

    public void setSellerFullName(String sellerFullName) {
        this.sellerFullName = sellerFullName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityCuit() {
        return entityCuit;
    }

    public void setEntityCuit(String entityCuit) {
        this.entityCuit = entityCuit;
    }

    public String getEntityCommercialAddress() {
        return entityCommercialAddress;
    }

    public void setEntityCommercialAddress(String entityCommercialAddress) {
        this.entityCommercialAddress = entityCommercialAddress;
    }

    public String getEntityGrossIncomeNumber() {
        return entityGrossIncomeNumber;
    }

    public void setEntityGrossIncomeNumber(String entityGrossIncomeNumber) {
        this.entityGrossIncomeNumber = entityGrossIncomeNumber;
    }

    public String getEntityVatCondition() {
        return entityVatCondition;
    }

    public void setEntityVatCondition(String entityVatCondition) {
        this.entityVatCondition = entityVatCondition;
    }

    public Date getEntityActivityStartDate() {
        return entityActivityStartDate;
    }

    public void setEntityActivityStartDate(Date entityActivityStartDate) {
        this.entityActivityStartDate = entityActivityStartDate;
    }

    public Integer getSalesPoint() {
        return salesPoint;
    }

    public void setSalesPoint(Integer salesPoint) {
        this.salesPoint = salesPoint;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Double discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public List<PaymentDetailResponseDTO> getPaymentDetails() {
        return paymentDetails;
    }

    public void setPaymentDetails(List<PaymentDetailResponseDTO> paymentDetails) {
        this.paymentDetails = paymentDetails;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }

    public List<InvoiceItemResponseDTO> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemResponseDTO> items) {
        this.items = items;
    }

    public String getCostingMethod() {
        return costingMethod;
    }

    public void setCostingMethod(String costingMethod) {
        this.costingMethod = costingMethod;
    }

    public Double getCmvAmount() {
        return cmvAmount;
    }

    public void setCmvAmount(Double cmvAmount) {
        this.cmvAmount = cmvAmount;
    }

    public String getLegalInvoiceNumber() {
        return legalInvoiceNumber;
    }

    public void setLegalInvoiceNumber(String legalInvoiceNumber) {
        this.legalInvoiceNumber = legalInvoiceNumber;
    }

    public String getCae() {
        return cae;
    }

    public void setCae(String cae) {
        this.cae = cae;
    }

    public Date getCaeExpirationDate() {
        return caeExpirationDate;
    }

    public void setCaeExpirationDate(Date caeExpirationDate) {
        this.caeExpirationDate = caeExpirationDate;
    }

    public String getQrCodeBase64() {
        return qrCodeBase64;
    }

    public void setQrCodeBase64(String qrCodeBase64) {
        this.qrCodeBase64 = qrCodeBase64;
    }
}
