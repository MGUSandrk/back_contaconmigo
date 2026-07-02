package com.sistema_contable.sistema.contable.dto.sales;

import com.sistema_contable.sistema.contable.model.DocumentType;
import com.sistema_contable.sistema.contable.model.VatCondition;

public class ClientRequestDTO {

    private String fullName;
    private String email;
    private String cuit;
    private VatCondition vatCondition;
    private DocumentType documentType;
    private String documentNumber;
    private String commercialAddress;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public VatCondition getVatCondition() {
        return vatCondition;
    }

    public void setVatCondition(VatCondition vatCondition) {
        this.vatCondition = vatCondition;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getCommercialAddress() {
        return commercialAddress;
    }

    public void setCommercialAddress(String commercialAddress) {
        this.commercialAddress = commercialAddress;
    }
}
