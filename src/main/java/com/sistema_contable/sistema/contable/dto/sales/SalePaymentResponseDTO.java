package com.sistema_contable.sistema.contable.dto.sales;

public class SalePaymentResponseDTO {

    private String method;
    private Double amount;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
