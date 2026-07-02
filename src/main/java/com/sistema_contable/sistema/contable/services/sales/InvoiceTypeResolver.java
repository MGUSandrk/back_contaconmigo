package com.sistema_contable.sistema.contable.services.sales;

import com.sistema_contable.sistema.contable.model.VatCondition;
import com.sistema_contable.sistema.contable.model.sales.InvoiceType;

class InvoiceTypeResolver {

    private InvoiceTypeResolver() {
    }

    public static InvoiceType resolve(VatCondition entityVatCondition, VatCondition clientVatCondition) {
        if (entityVatCondition == VatCondition.IVA_RESPONSABLE_INSCRIPTO) {
            return clientVatCondition == VatCondition.IVA_RESPONSABLE_INSCRIPTO
                    ? InvoiceType.A
                    : InvoiceType.B;
        }
        return InvoiceType.C;
    }
}
