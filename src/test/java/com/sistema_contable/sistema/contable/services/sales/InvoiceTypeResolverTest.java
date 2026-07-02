package com.sistema_contable.sistema.contable.services.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.sistema_contable.sistema.contable.model.VatCondition;
import com.sistema_contable.sistema.contable.model.sales.InvoiceType;

class InvoiceTypeResolverTest {

    @Test
    void responsibleRegisteredEntityAndResponsibleRegisteredClientResolvesInvoiceA() {
        InvoiceType invoiceType = InvoiceTypeResolver.resolve(
                VatCondition.IVA_RESPONSABLE_INSCRIPTO,
                VatCondition.IVA_RESPONSABLE_INSCRIPTO);

        assertEquals(InvoiceType.A, invoiceType);
    }

    @Test
    void responsibleRegisteredEntityAndConsumerClientResolvesInvoiceB() {
        InvoiceType invoiceType = InvoiceTypeResolver.resolve(
                VatCondition.IVA_RESPONSABLE_INSCRIPTO,
                VatCondition.CONSUMIDOR_FINAL);

        assertEquals(InvoiceType.B, invoiceType);
    }

    @Test
    void monotaxEntityResolvesInvoiceCForAnyClientCondition() {
        InvoiceType invoiceType = InvoiceTypeResolver.resolve(
                VatCondition.RESPONSABLE_MONOTRIBUTO,
                VatCondition.IVA_RESPONSABLE_INSCRIPTO);

        assertEquals(InvoiceType.C, invoiceType);
    }

    @Test
    void exemptEntityResolvesInvoiceCForAnyClientCondition() {
        InvoiceType invoiceType = InvoiceTypeResolver.resolve(
                VatCondition.IVA_EXENTO,
                VatCondition.IVA_RESPONSABLE_INSCRIPTO);

        assertEquals(InvoiceType.C, invoiceType);
    }
}
