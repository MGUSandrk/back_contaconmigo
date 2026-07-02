package com.sistema_contable.sistema.contable.resources;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistema_contable.sistema.contable.dto.sales.InvoiceResponseDTO;
import com.sistema_contable.sistema.contable.dto.sales.SaleRequestDTO;
import com.sistema_contable.sistema.contable.dto.sales.SaleResponseDTO;
import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.services.interfaces.SaleService;
import com.sistema_contable.sistema.contable.services.security.interfaces.AuthorizationService;

@RestController
@RequestMapping("/sales")
@CrossOrigin(origins = "${FRONT_URL}")
public class SaleResource {

    //dependencies
    @Autowired
    private SaleService saleService;
    @Autowired
    private AuthorizationService authService;

    //endpoints
    @PostMapping
    public ResponseEntity<?> createSale(@RequestHeader("Authorization") String token, @RequestBody SaleRequestDTO saleRequestDTO) {
        try {
            User seller = authService.authorize(token);
            InvoiceResponseDTO invoiceResponse = saleService.createSale(saleRequestDTO, seller);
            return new ResponseEntity<>(invoiceResponse, HttpStatus.CREATED);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllSales(@RequestHeader("Authorization") String token) {
        try {
            authService.authorize(token);
            List<SaleResponseDTO> sales = saleService.getAllSales();
            return new ResponseEntity<>(sales, HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<?> getSalesByClientId(@RequestHeader("Authorization") String token, @PathVariable Long clientId) {
        try {
            authService.authorize(token);
            List<SaleResponseDTO> sales = saleService.getSalesByClientId(clientId);
            return new ResponseEntity<>(sales, HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/invoices/client/{clientCuit}")
    public ResponseEntity<?> getInvoicesByClientCuit(@RequestHeader("Authorization") String token, @PathVariable String clientCuit) {
        try {
            authService.authorize(token);
            List<InvoiceResponseDTO> invoices = saleService.getInvoicesByClientCuit(clientCuit);
            return new ResponseEntity<>(invoices, HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
