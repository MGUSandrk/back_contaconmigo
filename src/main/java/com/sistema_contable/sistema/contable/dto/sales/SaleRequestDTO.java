package com.sistema_contable.sistema.contable.dto.sales;

import java.util.List;

import com.sistema_contable.sistema.contable.model.sales.InvoiceType;

public class SaleRequestDTO {

    private Long clientId;
    private List<SaleItemRequestDTO> items;
    private List<PaymentMethodDTO> payments;
    private Integer installments;
    private Double discount;
    private InvoiceType invoiceType;

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public List<SaleItemRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<SaleItemRequestDTO> items) {
        this.items = items;
    }

    public List<PaymentMethodDTO> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentMethodDTO> payments) {
        this.payments = payments;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public InvoiceType getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(InvoiceType invoiceType) {
        this.invoiceType = invoiceType;
    }
}
