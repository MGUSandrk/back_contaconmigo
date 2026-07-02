package com.sistema_contable.sistema.contable.services.interfaces;

import com.sistema_contable.sistema.contable.model.sales.Invoice;

public interface InvoiceService {

    void create(Invoice invoice) throws Exception;
    Invoice searchById(Long id) throws Exception;
    byte[] generatePdf(Long id) throws Exception;
}
