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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sistema_contable.sistema.contable.dto.sales.PaymentTypeRequestDTO;
import com.sistema_contable.sistema.contable.dto.sales.PaymentTypeResponseDTO;
import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;
import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.model.sales.PaymentType;
import com.sistema_contable.sistema.contable.services.interfaces.PaymentTypeService;
import com.sistema_contable.sistema.contable.services.security.interfaces.AuthorizationService;

@RestController
@RequestMapping("/payment-types")
@CrossOrigin(origins = "${FRONT_URL}")
public class PaymentTypeResource {

    //dependencies
    @Autowired
    private PaymentTypeService service;
    @Autowired
    private AuthorizationService authService;

    //endpoints
    @PostMapping(path = "/create")
    public ResponseEntity<?> create(@RequestHeader("Authorization") String token, @RequestBody PaymentTypeRequestDTO paymentTypeDTO) {
        try {
            authService.adminAuthorize(token);
            service.create(paymentTypeRequest(paymentTypeDTO));
            return new ResponseEntity<>(null, HttpStatus.CREATED);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> getAll(@RequestHeader("Authorization") String token) {
        try {
            authService.sellerAuthorize(token);
            return new ResponseEntity<>(paymentTypeResponse(service.getAll()), HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/{id}", produces = "application/json")
    public ResponseEntity<?> getById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            authService.sellerAuthorize(token);
            return new ResponseEntity<>(paymentTypeResponse(service.searchById(id)), HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifyById(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody PaymentTypeRequestDTO paymentTypeDTO) {
        try {
            authService.adminAuthorize(token);
            service.modifyById(id, paymentTypeRequest(paymentTypeDTO));
            return new ResponseEntity<>(null, HttpStatus.RESET_CONTENT);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        try {
            authService.adminAuthorize(token);
            service.deleteById(id);
            return new ResponseEntity<>(null, HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //secondary methods
    private PaymentType paymentTypeRequest(PaymentTypeRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        PaymentType paymentType = new PaymentType();
        paymentType.setType(dto.getType());
        if (dto.getAccountId() != null) {
            BalanceAccount account = new BalanceAccount();
            account.setId(dto.getAccountId());
            paymentType.setAccount(account);
        }
        return paymentType;
    }

    private List<PaymentTypeResponseDTO> paymentTypeResponse(List<PaymentType> paymentTypes) throws Exception {
        List<PaymentTypeResponseDTO> dtos = new ArrayList<>();
        for (PaymentType paymentType : paymentTypes) {
            dtos.add(paymentTypeResponse(paymentType));
        }
        return dtos;
    }

    private PaymentTypeResponseDTO paymentTypeResponse(PaymentType paymentType) throws Exception {
        PaymentTypeResponseDTO dto = new PaymentTypeResponseDTO();
        dto.setId(paymentType.getId());
        dto.setType(paymentType.getType());
        if (paymentType.getAccount() != null) {
            dto.setAccountId(paymentType.getAccount().getId());
            dto.setAccountName(paymentType.getAccount().getName());
            dto.setAccountCode(paymentType.getAccount().getCode());
        }
        dto.setBalance(service.currentBalance(paymentType));
        return dto;
    }
}
