package com.sistema_contable.sistema.contable.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImpTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private GoodsAccountingService goodsAccountingService;

    @InjectMocks
    private ProductServiceImp service;

    @Test
    void getAllWithStockReturnsRepositoryProducts() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Notebook");
        product.setSalePrice(1200.0);
        List<Product> products = List.of(product);

        when(repository.findAllWithStock()).thenReturn(products);

        List<Product> result = service.getAllWithStock();

        assertEquals(products, result);
        verify(repository).findAllWithStock();
        verifyNoMoreInteractions(repository);
    }
}
