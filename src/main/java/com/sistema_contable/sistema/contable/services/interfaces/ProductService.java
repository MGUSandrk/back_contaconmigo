package com.sistema_contable.sistema.contable.services.interfaces;

import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.model.Lot;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.sales.Payment;

import java.util.List;

public interface ProductService {
    void create(Product product) throws Exception;
    Product create(Product product, List<Payment> payments, User user) throws Exception;
    Product addLot(Long productId, Lot lot, List<Payment> payments, User user) throws Exception;
    List<Product> getAll() throws Exception;
    List<Product> getAllWithStock() throws Exception;
    Product searchById(Long id) throws Exception;
    Product searchByName(String name) throws Exception;
    Integer totalStock(Product product);
    void delete(Long id) throws Exception;
}
