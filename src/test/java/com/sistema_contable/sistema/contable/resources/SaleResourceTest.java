package com.sistema_contable.sistema.contable.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.sistema_contable.sistema.contable.dto.sales.SaleResponseDTO;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.services.interfaces.SaleService;
import com.sistema_contable.sistema.contable.services.security.interfaces.AuthorizationService;

class SaleResourceTest {

    @Test
    void getSalesByDateAuthorizesAndReturnsSales() throws Exception {
        SaleService service = mock(SaleService.class);
        AuthorizationService authService = mock(AuthorizationService.class);
        SaleResource resource = new SaleResource();
        ReflectionTestUtils.setField(resource, "saleService", service);
        ReflectionTestUtils.setField(resource, "authService", authService);
        List<SaleResponseDTO> sales = List.of(new SaleResponseDTO());

        when(authService.authorize("Bearer token")).thenReturn(new User());
        when(service.getSalesByDate(7, 2026)).thenReturn(sales);

        ResponseEntity<?> response = resource.getSalesByDate("Bearer token", 7, 2026);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sales, response.getBody());
        verify(authService).authorize("Bearer token");
        verify(service).getSalesByDate(7, 2026);
    }

    @Test
    void countSalesOfCurrentMonthAuthorizesAndReturnsCount() throws Exception {
        SaleService service = mock(SaleService.class);
        AuthorizationService authService = mock(AuthorizationService.class);
        SaleResource resource = new SaleResource();
        ReflectionTestUtils.setField(resource, "saleService", service);
        ReflectionTestUtils.setField(resource, "authService", authService);

        when(authService.authorize("Bearer token")).thenReturn(new User());
        when(service.countSalesOfCurrentMonth()).thenReturn(7L);

        ResponseEntity<?> response = resource.countSalesOfCurrentMonth("Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Map.of("count", 7L), response.getBody());
        verify(authService).authorize("Bearer token");
        verify(service).countSalesOfCurrentMonth();
    }

    @Test
    void getMonthlySalesReportPdfAuthorizesAndReturnsPdfInline() throws Exception {
        SaleService service = mock(SaleService.class);
        AuthorizationService authService = mock(AuthorizationService.class);
        SaleResource resource = new SaleResource();
        ReflectionTestUtils.setField(resource, "saleService", service);
        ReflectionTestUtils.setField(resource, "authService", authService);
        byte[] pdf = "pdf".getBytes();

        when(authService.authorize("Bearer token")).thenReturn(new User());
        when(service.generateMonthlySalesReportPdf(7, 2026)).thenReturn(pdf);

        ResponseEntity<?> response = resource.getMonthlySalesReportPdf("Bearer token", 7, 2026, false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals("inline; filename=\"sales-report-2026-07.pdf\"", response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals(pdf, response.getBody());
        verify(authService).authorize("Bearer token");
        verify(service).generateMonthlySalesReportPdf(7, 2026);
    }
}
