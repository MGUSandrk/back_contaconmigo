package com.sistema_contable.sistema.contable.services.sales;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sistema_contable.sistema.contable.dto.sales.SaleResponseDTO;
import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.sales.Client;
import com.sistema_contable.sistema.contable.model.sales.Payment;
import com.sistema_contable.sistema.contable.model.sales.PaymentType;
import com.sistema_contable.sistema.contable.model.sales.Sale;
import com.sistema_contable.sistema.contable.model.sales.SaleProduct;
import com.sistema_contable.sistema.contable.repository.SaleRepository;

@ExtendWith(MockitoExtension.class)
class SaleServiceImpTest {

    @Mock
    private SaleRepository saleRepository;
    @Mock
    private SalesReportPdfService salesReportPdfService;

    @InjectMocks
    private SaleServiceImp service;

    @Test
    void generateMonthlySalesReportPdfSummarizesPaymentsByTypeAndDelegatesPdfGeneration() throws Exception {
        Date startDate = date(2026, Calendar.JULY, 1, 0, 0, 0, 0);
        Date endDate = date(2026, Calendar.AUGUST, 1, 0, 0, 0, 0);
        Sale cashSale = sale(33L, "Efectivo", 2400.0);
        Sale cardSale = sale(34L, "Tarjeta", 1500.0);
        Sale otherCashSale = sale(35L, "Efectivo", 600.0);
        byte[] pdf = "pdf".getBytes();
        when(saleRepository.findByDateCreatedBetween(startDate, endDate)).thenReturn(List.of(cashSale, cardSale, otherCashSale));
        when(salesReportPdfService.generarPdf(
                eq(7),
                eq(2026),
                anyList(),
                anyMap(),
                anyDouble()
        )).thenReturn(pdf);

        byte[] result = service.generateMonthlySalesReportPdf(7, 2026);

        assertEquals(pdf, result);
        verify(saleRepository).findByDateCreatedBetween(startDate, endDate);
        ArgumentCaptor<List<SaleResponseDTO>> salesCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<Map<String, Double>> totalsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Double> totalCaptor = ArgumentCaptor.forClass(Double.class);
        verify(salesReportPdfService).generarPdf(
                eq(7),
                eq(2026),
                salesCaptor.capture(),
                totalsCaptor.capture(),
                totalCaptor.capture()
        );
        assertEquals(3, salesCaptor.getValue().size());
        assertEquals(Map.of("Efectivo", 3000.0, "Tarjeta", 1500.0), totalsCaptor.getValue());
        assertEquals(4500.0, totalCaptor.getValue());
    }

    @Test
    void getSalesByDateReturnsSalesBetweenMonthStartAndNextMonthStartWithProductsAndPayments() throws Exception {
        Date startDate = date(2026, Calendar.JULY, 1, 0, 0, 0, 0);
        Date endDate = date(2026, Calendar.AUGUST, 1, 0, 0, 0, 0);
        Sale sale = sale(33L, "Efectivo", 2400.0);
        when(saleRepository.findByDateCreatedBetween(startDate, endDate)).thenReturn(List.of(sale));

        List<SaleResponseDTO> result = service.getSalesByDate(7, 2026);

        assertEquals(1, result.size());
        assertEquals(33L, result.get(0).getId());
        assertEquals("Ana Perez", result.get(0).getClientFullName());
        assertEquals("seller", result.get(0).getSellerUsername());
        assertEquals("Yerba", result.get(0).getProducts().get(0).getProductName());
        assertEquals(2, result.get(0).getProducts().get(0).getQuantity());
        assertEquals("Efectivo", result.get(0).getPayments().get(0).getMethod());
        assertEquals(2400.0, result.get(0).getPayments().get(0).getAmount());
        verify(saleRepository).findByDateCreatedBetween(startDate, endDate);
    }

    @Test
    void countSalesOfCurrentMonthCountsSalesBetweenMonthStartAndNextMonthStart() throws Exception {
        when(saleRepository.countSalesByDateCreatedBetween(
                expectedMonthStart(),
                expectedNextMonthStart()
        )).thenReturn(7L);

        Long result = service.countSalesOfCurrentMonth();

        assertEquals(7L, result);
        ArgumentCaptor<Date> startDateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> endDateCaptor = ArgumentCaptor.forClass(Date.class);
        verify(saleRepository).countSalesByDateCreatedBetween(startDateCaptor.capture(), endDateCaptor.capture());
        assertEquals(expectedMonthStart(), startDateCaptor.getValue());
        assertEquals(expectedNextMonthStart(), endDateCaptor.getValue());
    }

    private Date expectedMonthStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date expectedNextMonthStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(expectedMonthStart());
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private Sale sale(Long id, String paymentMethod, Double amount) {
        Client client = new Client();
        client.setId(8L);
        client.setFullName("Ana Perez");

        User seller = new User();
        seller.setId(5L);
        seller.setUsername("seller");

        Product product = new Product();
        product.setName("Yerba");

        SaleProduct saleProduct = new SaleProduct();
        saleProduct.setProduct(product);
        saleProduct.setQuantity(2);

        PaymentType paymentType = new PaymentType();
        paymentType.setType(paymentMethod);

        Payment payment = new Payment();
        payment.setPaymentType(paymentType);
        payment.setAmount(amount);

        Sale sale = new Sale();
        sale.setId(id);
        sale.setDateCreated(date(2026, Calendar.JULY, 10, 11, 30, 0, 0));
        sale.setClient(client);
        sale.setSeller(seller);
        sale.setSaleProducts(List.of(saleProduct));
        sale.setPayments(List.of(payment));
        sale.setTotalPrice(amount);
        return sale;
    }

    private Date date(int year, int month, int day, int hour, int minute, int second, int millisecond) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, hour, minute, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        return calendar.getTime();
    }
}
