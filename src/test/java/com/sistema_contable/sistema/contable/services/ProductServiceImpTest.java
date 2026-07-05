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
    void updateOnlyChangesNameAndSalePrice() throws Exception {
        Product storedProduct = new Product();
        storedProduct.setId(1L);
        storedProduct.setName("Yerba");
        storedProduct.setSalePrice(1200.0);

        Product changes = new Product();
        changes.setName("Yerba Especial");
        changes.setSalePrice(1500.0);

        when(repository.searchById(1L)).thenReturn(storedProduct);
        when(repository.searchByName("Yerba Especial")).thenReturn(null);
        when(repository.save(storedProduct)).thenReturn(storedProduct);

        Product result = service.update(1L, changes);

        assertEquals(storedProduct, result);
        assertEquals("Yerba Especial", storedProduct.getName());
        assertEquals(1500.0, storedProduct.getSalePrice());
        verify(repository).searchById(1L);
        verify(repository).searchByName("Yerba Especial");
        verify(repository).save(storedProduct);
    }

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
