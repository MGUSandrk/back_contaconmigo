package com.sistema_contable.sistema.contable.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.sistema_contable.sistema.contable.dto.ProductResponseDTO;
import com.sistema_contable.sistema.contable.model.Lot;
import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.services.interfaces.ProductService;
import com.sistema_contable.sistema.contable.services.security.interfaces.AuthorizationService;

class ProductResourceTest {

    @Test
    void getAllWithStockAuthorizesSellerAndReturnsProductResponses() throws Exception {
        ProductService service = mock(ProductService.class);
        AuthorizationService authService = mock(AuthorizationService.class);
        ProductResource resource = new ProductResource();
        ReflectionTestUtils.setField(resource, "service", service);
        ReflectionTestUtils.setField(resource, "authService", authService);

        Product product = new Product();
        product.setId(1L);
        product.setName("Notebook");
        product.setSalePrice(1200.0);
        Lot lot = new Lot();
        lot.setId(10L);
        lot.setUnitPrice(700.0);
        lot.setStock(3);
        product.addLot(lot);

        when(authService.sellerAuthorize("Bearer token")).thenReturn(new User());
        when(service.getAllWithStock()).thenReturn(List.of(product));
        when(service.totalStock(product)).thenReturn(3);

        ResponseEntity<?> response = resource.getAllWithStock("Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<ProductResponseDTO> body = (List<ProductResponseDTO>) response.getBody();
        assertEquals(1, body.size());
        assertEquals(1L, body.get(0).getId());
        assertEquals("Notebook", body.get(0).getName());
        assertEquals(1200.0, body.get(0).getSalePrice());
        assertEquals(3, body.get(0).getTotalStock());
        assertEquals(1, body.get(0).getLots().size());
        assertEquals(10L, body.get(0).getLots().get(0).getId());
        assertEquals(3, body.get(0).getLots().get(0).getStock());
        assertEquals(1L, body.get(0).getLots().get(0).getProductId());
        verify(authService).sellerAuthorize("Bearer token");
        verify(service).getAllWithStock();
        verify(service).totalStock(product);
    }
}
