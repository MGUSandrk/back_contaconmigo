package com.sistema_contable.sistema.contable.resources;

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

import com.sistema_contable.sistema.contable.dto.ClientRequestDTO;
import com.sistema_contable.sistema.contable.dto.ClientResponseDTO;
import com.sistema_contable.sistema.contable.exceptions.ModelExceptions;
import com.sistema_contable.sistema.contable.model.Client;
import com.sistema_contable.sistema.contable.services.interfaces.ClientService;
import com.sistema_contable.sistema.contable.services.security.interfaces.AuthorizationService;

@RestController
@RequestMapping("/clients")
@CrossOrigin(origins = "${FRONT_URL}")
public class ClientResource {

    //dependencies
    @Autowired
    private ClientService service;
    @Autowired
    private AuthorizationService authService;

    //endpoints
    @PostMapping(path = "/create")
    public ResponseEntity<?> create(@RequestHeader("Authorization") String token, @RequestBody ClientRequestDTO clientDTO) {
        try {
            authService.authorize(token);
            service.create(clientRequest(clientDTO));
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
            return new ResponseEntity<>(clientResponse(service.getAll()), HttpStatus.OK);
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
            return new ResponseEntity<>(clientResponse(service.searchById(id)), HttpStatus.OK);
        } catch (ModelExceptions modelError) {
            System.out.println(modelError.getMessage());
            return new ResponseEntity<>(null, modelError.getHttpStatus());
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> modifyById(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody ClientRequestDTO clientDTO) {
        try {
            authService.adminAuthorize(token);
            service.modifyById(id, clientRequest(clientDTO));
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
    private Client clientRequest(ClientRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Client client = new Client();
        client.setFullName(dto.getFullName());
        client.setEmail(dto.getEmail());
        client.setCuit(dto.getCuit());
        client.setVatCondition(dto.getVatCondition());
        client.setDocumentType(dto.getDocumentType());
        client.setDocumentNumber(dto.getDocumentNumber());
        client.setCommercialAddress(dto.getCommercialAddress());
        return client;
    }

    private List<ClientResponseDTO> clientResponse(List<Client> clients) {
        return clients.stream().map(this::clientResponse).toList();
    }

    private ClientResponseDTO clientResponse(Client client) {
        ClientResponseDTO dto = new ClientResponseDTO();
        dto.setId(client.getId());
        dto.setFullName(client.getFullName());
        dto.setEmail(client.getEmail());
        dto.setCuit(client.getCuit());
        dto.setVatCondition(client.getVatCondition());
        dto.setDocumentType(client.getDocumentType());
        dto.setDocumentNumber(client.getDocumentNumber());
        dto.setCommercialAddress(client.getCommercialAddress());
        return dto;
    }
}
