package com.sistema_contable.sistema.contable.services.sales;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistema_contable.sistema.contable.exceptions.sales.InvoiceNotFindException;
import com.sistema_contable.sistema.contable.model.sales.Invoice;
import com.sistema_contable.sistema.contable.repository.InvoiceRepository;
import com.sistema_contable.sistema.contable.services.interfaces.InvoiceService;

@Service
public class InvoiceServiceImp implements InvoiceService {

    //dependencies
    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private InvoicePdfService invoicePdfService;

    //CRUD
    @Override
    public void create(Invoice invoice) throws Exception {
        invoiceRepository.save(invoice);
    }

    //SEARCHES
    @Override
    @Transactional(readOnly = true)
    public Invoice searchById(Long id) throws Exception {
        Invoice invoice = invoiceRepository.searchById(id);
        if (invoice == null) {
            throw new InvoiceNotFindException("ERROR : Invoice not found by id");
        }
        return invoice;
    }

    //SECONDARY METHODS
    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) throws Exception {
        Invoice invoice = searchById(id);
        return invoicePdfService.generarPdf(invoice);
    }
}
