package com.sistema_contable.sistema.contable.dto.sales;

public class PaymentTypeRequestDTO {

    private String type;
    private Long accountId;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
}
