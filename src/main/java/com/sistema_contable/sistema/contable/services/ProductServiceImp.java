package com.sistema_contable.sistema.contable.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistema_contable.sistema.contable.exceptions.BadLotException;
import com.sistema_contable.sistema.contable.exceptions.BadProductException;
import com.sistema_contable.sistema.contable.exceptions.ProductNotFindException;
import com.sistema_contable.sistema.contable.model.Lot;
import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.sales.Payment;
import com.sistema_contable.sistema.contable.repository.ProductRepository;
import com.sistema_contable.sistema.contable.services.interfaces.ProductService;

@Service
public class ProductServiceImp implements ProductService {

    //dependencies
    @Autowired
    private ProductRepository repository;
    @Autowired
    private GoodsAccountingService goodsAccountingService;

    //CRUD
    @Override
    public void create(Product product) throws Exception {
        saveNewProduct(product);
    }

    @Override
    @Transactional
    public Product create(Product product, List<Payment> payments, User user) throws Exception {
        Lot lot = product.getLots().get(0);
        validatePayments(payments, lot);
        goodsAccountingService.purchaseLotAccounting(payments, product, lot, user);
        Product savedProduct = saveNewProduct(product);
        return savedProduct;
    }

    @Override
    @Transactional
    public Product addLot(Long productId, Lot lot, List<Payment> payments, User user) throws Exception {
        Product product = searchById(productId);
        validateLot(lot);
        validatePayments(payments, lot);
        product.addLot(lot);
        Product savedProduct = repository.save(product);
        goodsAccountingService.purchaseLotAccounting(payments, savedProduct, lot, user);
        return savedProduct;
    }

    private Product saveNewProduct(Product product) throws Exception {
        validateProduct(product);
        String name = product.getName().strip();
        String formatName = name.substring(0, 1).toUpperCase() + name.substring(1);
        product.setName(formatName);
        if (this.searchByName(product.getName()) != null) {
            throw new BadProductException("ERROR : Found product with same name");
        }
        
        for (Lot lot : product.getLots()) {
            this.validateLot(lot);
            lot.setProduct(product);
        }
        return repository.save(product);
    }

    @Override
    public void delete(Long id) throws Exception {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new ProductNotFindException("ERROR : Product not found to DELETE");
        }
    }

    //GETTERS
    @Override
    public List<Product> getAll() throws Exception {
        return repository.findAll();
    }

    @Override
    public List<Product> getAllWithStock() throws Exception {
        return repository.findAllWithStock();
    }

    //SEARCHES
    @Override
    public Product searchById(Long id) throws Exception {
        Product product = repository.searchById(id);
        if (product == null) {
            throw new ProductNotFindException("ERROR : Product not found by id");
        }
        return product;
    }

    @Override
    public Product searchByName(String name) throws Exception {
        return repository.searchByName(name);
    }

    @Override
    public Integer totalStock(Product product) {
        if (product == null || product.getLots() == null) {
            return 0;
        }
        return product.getLots().stream()
                .filter(lot -> lot != null && lot.getStock() != null)
                .mapToInt(Lot::getStock)
                .sum();
    }

    //SECONDARY METHODS
    private void validateProduct(Product product) throws Exception {
        if (product == null) {
            throw new BadProductException("ERROR : Product is required");
        }
        if (product.getName() == null || product.getName().isBlank()) {
            throw new BadProductException("ERROR : Product name is required");
        }
        if (product.getSalePrice() == null || product.getSalePrice() < 0) {
            throw new BadProductException("ERROR : Product sale price is invalid");
        }
        if (product.getLots() == null || product.getLots().size() != 1) {
            throw new BadProductException("ERROR : Product needs one initial lot");
        }
    }

    private void validateLot(Lot lot) throws Exception {
        if (lot == null) {
            throw new BadLotException("ERROR : Lot is required");
        }
        if (lot.getUnitPrice() == null || lot.getUnitPrice() < 0) {
            throw new BadLotException("ERROR : Lot unit price is invalid");
        }
        if (lot.getStock() == null || lot.getStock() < 0) {
            throw new BadLotException("ERROR : Lot stock is invalid");
        }
    }

    private void validatePayments(List<Payment> payments, Lot lot) throws Exception {
        if (payments == null || payments.isEmpty()) {
            throw new BadProductException("ERROR : Product lot payments are required");
        }
        Double totalPayments = 0.0;
        for (Payment payment : payments) {
            if (payment == null || payment.getAmount() == null || payment.getAmount() < 0) {
                throw new BadProductException("ERROR : Product lot payment amount is invalid");
            }
            if (payment.getPaymentType() == null || payment.getPaymentType().getAccount() == null) {
                throw new BadProductException("ERROR : Product lot payment type is invalid");
            }
            totalPayments += payment.getAmount();
        }
        Double lotCost = lotCost(lot);
        if (Math.abs(totalPayments - lotCost) > 0.01) {
            throw new BadProductException("ERROR : Product lot payments total does not match lot cost");
        }
    }

    private Double lotCost(Lot lot) {
        return lot.getUnitPrice() * lot.getStock();
    }
}
