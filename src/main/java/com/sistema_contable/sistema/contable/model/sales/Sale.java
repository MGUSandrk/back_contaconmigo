package com.sistema_contable.sistema.contable.model.sales;

import java.util.Date;
import java.util.List;

import com.sistema_contable.sistema.contable.model.EntityModel;
import com.sistema_contable.sistema.contable.model.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "sales")
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sale")
    private Long id;

    @Column(name = "date_created")
    private Date dateCreated;

    @ManyToOne
    @JoinColumn(name = "sale_client_id")
    private Client client;

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne
    @JoinColumn(name = "sale_entity_id")
    private EntityModel entity;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "sale_product_sale_id")
    private List<SaleProduct> saleProducts;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "payment_sale_id")
    private List<Payment> payments;

    @Column(name = "total_price")
    private Double totalPrice;

    public Sale(){}

    public Sale(Date dateCreated, Client client, User seller, List<SaleProduct> saleProducts, List<Payment> payments) {
        this.dateCreated = dateCreated;
        this.client = client;
        this.seller = seller;
        this.saleProducts = saleProducts;
        this.payments = payments;
    }

    public void addPayment(Payment payment){
        this.getPayments().add(payment);
    }

    public void deletePayment(Payment payment){
        this.getPayments().remove(payment);
    }

    public void addSaleProduct(SaleProduct saleProduct){
        this.getSaleProducts().add(saleProduct);
    }

    public void deleteSaleProduct(SaleProduct saleProduct){
        this.getSaleProducts().remove(saleProduct);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public EntityModel getEntity() {
        return entity;
    }

    public void setEntity(EntityModel entity) {
        this.entity = entity;
    }

    public List<SaleProduct> getSaleProducts() {
        return saleProducts;
    }

    public void setSaleProducts(List<SaleProduct> saleProducts) {
        this.saleProducts = saleProducts;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

}
