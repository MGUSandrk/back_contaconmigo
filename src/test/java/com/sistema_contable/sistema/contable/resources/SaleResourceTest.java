package com.sistema_contable.sistema.contable.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.services.interfaces.SaleService;
import com.sistema_contable.sistema.contable.services.security.interfaces.AuthorizationService;

class SaleResourceTest {

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
        assertEquals(7L, response.getBody());
        verify(authService).authorize("Bearer token");
        verify(service).countSalesOfCurrentMonth();
    }
}
