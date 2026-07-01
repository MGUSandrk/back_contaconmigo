package com.sistema_contable.sistema.contable.model.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sistema_contable.sistema.contable.model.Client;
import com.sistema_contable.sistema.contable.model.CostingMethodType;
import com.sistema_contable.sistema.contable.model.EntityModel;
import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.VatCondition;

public class InvoiceTest {

    @Test
    void fromSaleStoresInvoiceTypeEntitySnapshotAndItems() {
        Sale sale = new Sale();
        sale.setId(3L);
        sale.setDateCreated(new Date());

        Product product = new Product();
        product.setId(7L);
        product.setName("Lapicera azul");

        SaleProduct saleProduct = new SaleProduct();
        saleProduct.setProduct(product);
        saleProduct.setQuantity(2);
        saleProduct.setPrice(150.0);
        sale.setSaleProducts(List.of(saleProduct));

        Client client = new Client();
        client.setFullName("Juan Perez");
        client.setCuit("20123456789");
        client.setVatCondition(VatCondition.CONSUMIDOR_FINAL);
        client.setCommercialAddress("Calle Cliente 123");

        User seller = new User();
        seller.setUsername("vendedor");

        EntityModel entity = new EntityModel();
        entity.setName("Empresa SA");
        entity.setCuit("30711222333");
        entity.setCommercialAddress("Calle Empresa 456");
        entity.setGrossIncomeNumber("902-123456-7");
        entity.setVatCondition(VatCondition.IVA_RESPONSABLE_INSCRIPTO);
        entity.setActivityStartDate(new Date());
        entity.setSalesPoint(4);
        entity.setCostingMethod(CostingMethodType.FIFO);

        Invoice invoice = Invoice.fromSale(
                sale,
                client,
                seller,
                entity,
                InvoiceType.A,
                "Efectivo",
                1,
                300.0,
                0.0,
                300.0,
                90.0);

        assertEquals(InvoiceType.A, invoice.getInvoiceType());
        assertEquals("Empresa SA", invoice.getEntityName());
        assertEquals("30711222333", invoice.getEntityCuit());
        assertEquals("Calle Empresa 456", invoice.getEntityCommercialAddress());
        assertEquals("902-123456-7", invoice.getEntityGrossIncomeNumber());
        assertEquals("IVA_RESPONSABLE_INSCRIPTO", invoice.getEntityVatCondition());
        assertEquals(4, invoice.getSalesPoint());
        assertEquals(1, invoice.getItems().size());
        assertEquals("Lapicera azul", invoice.getItems().get(0).getProductName());
        assertEquals(300.0, invoice.getItems().get(0).getSubtotal());
        assertEquals("00004-00000003", invoice.getLegalInvoiceNumber());
        assertEquals("70417054367476", invoice.getCae());
        assertEquals("data:image/png;base64,", invoice.getQrCodeBase64().substring(0, 22));
        assertTrue(invoice.getCaeExpirationDate().after(invoice.getDateCreated()));
    }
}
