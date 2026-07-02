package com.sistema_contable.sistema.contable.model.sales;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_details")
public class PaymentDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment_detail")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    @Column(name = "method")
    private String method;

    @Column(name = "amount")
    private Double amount;

    public PaymentDetail() {
    }

    public PaymentDetail(Invoice invoice, String method, Double amount) {
        this.invoice = invoice;
        this.method = method;
        this.amount = amount;
    }

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getMethod() {
        return method;
    }

    public Double getAmount() {
        return amount;
    }
}
