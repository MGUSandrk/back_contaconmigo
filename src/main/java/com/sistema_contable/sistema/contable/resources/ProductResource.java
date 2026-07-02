package com.sistema_contable.sistema.contable.resources;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistema_contable.sistema.contable.dto.LotRequestDTO;
import com.sistema_contable.sistema.contable.dto.LotResponseDTO;
import com.sistema_contable.sistema.contable.dto.ProductLotRequestDTO;
import com.sistema_contable.sistema.contable.dto.ProductRequestDTO;
import com.sistema_contable.sistema.contable.dto.ProductResponseDTO;
import com.sistema_contable.sistema.contable.dto.sales.PaymentRequestDTO;
import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;
import com.sistema_contable.sistema.contable.model.Lot;
import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.sales.Payment;
import com.sistema_contable.sistema.contable.services.interfaces.PaymentTypeService;
import com.sistema_contable.sistema.contable.services.interfaces.ProductService;
import com.sistema_contable.sistema.contable.services.security.interfaces.AuthorizationService;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "${FRONT_URL}")
public class ProductResource {

    //dependencies
    @Autowired
    private ProductService service;
    @Autowired
    private AuthorizationService authService;
    @Autowired
    private PaymentTypeService paymentTypeService;

    //endpoints
    @PostMapping(path = "/create")
    public ResponseEntity<?> create(@RequestHeader("Authorization") String token, @RequestBody ProductRequestDTO productDTO) {
        try {
            User userDB = authService.adminAuthorize(token);
            Product product = productRequest(productDTO);
            List<Payment> payments = paymentRequest(productDTO != null ? productDTO.getPayments() : null);
            service.create(product, payments, userDB);
            return new ResponseEntity<>(null, HttpStatus.CREATED);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(path = "/{id}/lot")
    public ResponseEntity<?> addLot(@RequestHeader("Authorization") String token, @PathVariable Long id,
            @RequestBody ProductLotRequestDTO dto) {
        try {
            User userDB = authService.adminAuthorize(token);
            Lot lot = lotRequest(dto != null ? dto.getLot() : null);
            List<Payment> payments = paymentRequest(dto != null ? dto.getPayments() : null);
            service.addLot(id, lot, payments, userDB);
            return new ResponseEntity<>(null, HttpStatus.CREATED);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String token) {
        try {
            authService.sellerAuthorize(token);
            return new ResponseEntity<>(productResponse(service.getAll()), HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/with-stock", produces = "application/json")
    public ResponseEntity<?> getAllWithStock(@RequestHeader("Authorization") String token) {
        try {
            authService.sellerAuthorize(token);
            return new ResponseEntity<>(productResponse(service.getAllWithStock()), HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            authService.adminAuthorize(token);
            service.delete(id);
            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //secondary methods
    private Product productRequest(ProductRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Product product = new Product();
        product.setName(dto.getName());
        product.setSalePrice(dto.getSalePrice());
        Lot lot = lotRequest(dto.getLot());
        if (lot != null) {
            product.addLot(lot);
        }
        return product;
    }

    private Lot lotRequest(LotRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Lot lot = new Lot();
        lot.setUnitPrice(dto.getUnitPrice());
        lot.setStock(dto.getStock());
        return lot;
    }

    private List<Payment> paymentRequest(List<PaymentRequestDTO> dtos) throws Exception {
        if (dtos == null) {
            return null;
        }
        List<Payment> payments = new ArrayList<>();
        for (PaymentRequestDTO dto : dtos) {
            payments.add(paymentRequest(dto));
        }
        return payments;
    }

    private Payment paymentRequest(PaymentRequestDTO dto) throws Exception {
        if (dto == null) {
            return null;
        }
        Payment payment = new Payment();
        payment.setAmount(dto.getAmount());
        if (dto.getId() != null) {
            payment.setPaymentType(paymentTypeService.searchById(dto.getId()));
        }
        return payment;
    }

    private List<ProductResponseDTO> productResponse(List<Product> products) {
        return products.stream().map(this::productResponse).toList();
    }

    private ProductResponseDTO productResponse(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSalePrice(product.getSalePrice());
        dto.setTotalStock(service.totalStock(product));
        dto.setLots(product.getLots().stream().map(this::lotResponse).toList());
        return dto;
    }

    private LotResponseDTO lotResponse(Lot lot) {
        LotResponseDTO dto = new LotResponseDTO();
        dto.setId(lot.getId());
        dto.setUnitPrice(lot.getUnitPrice());
        dto.setStock(lot.getStock());
        if (lot.getProduct() != null) {
            dto.setProductId(lot.getProduct().getId());
        }
        return dto;
    }
}
