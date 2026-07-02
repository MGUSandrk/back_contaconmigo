package com.sistema_contable.sistema.contable.dto.sales;

public class PaymentRequestDTO {

    private Long id;
    private Double amount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }
}
