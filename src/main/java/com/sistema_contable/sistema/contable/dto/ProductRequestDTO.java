package com.sistema_contable.sistema.contable.dto;

import java.util.List;

import com.sistema_contable.sistema.contable.dto.sales.PaymentRequestDTO;

public class ProductRequestDTO {

    private String name;
    private Double salePrice;
    private LotRequestDTO lot;
    private List<PaymentRequestDTO> payments;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice = salePrice;
    }

    public LotRequestDTO getLot() {
        return lot;
    }

    public void setLot(LotRequestDTO lot) {
        this.lot = lot;
    }

    public List<PaymentRequestDTO> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentRequestDTO> payments) {
        this.payments = payments;
    }
}
