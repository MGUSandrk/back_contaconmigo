package com.sistema_contable.sistema.contable.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sistema_contable.sistema.contable.exceptions.sales.BadClientException;
import com.sistema_contable.sistema.contable.exceptions.sales.ClientNotFindException;
import com.sistema_contable.sistema.contable.model.VatCondition;
import com.sistema_contable.sistema.contable.model.sales.Client;
import com.sistema_contable.sistema.contable.repository.ClientRepository;
import com.sistema_contable.sistema.contable.services.interfaces.ClientService;

@Service
public class ClientServiceImp implements ClientService {

    //dependencies
    @Autowired
    private ClientRepository repository;

    //CRUD
    @Override
    public void create(Client client) throws Exception {
        this.validateClient(client);
        normalizeClient(client);
        repository.save(client);
    }

    @Override
    public void modifyById(Long id, Client client) throws Exception {
        this.validateClient(client);
        Client storedClient = this.searchById(id);
        storedClient.setFullName(normalize(client.getFullName()));
        storedClient.setEmail(normalize(client.getEmail()));
        storedClient.setCuit(normalize(client.getCuit()));
        storedClient.setVatCondition(client.getVatCondition());
        storedClient.setDocumentType(client.getDocumentType());
        storedClient.setDocumentNumber(normalize(client.getDocumentNumber()));
        storedClient.setCommercialAddress(normalize(client.getCommercialAddress()));
        repository.save(storedClient);
    }

    @Override
    public void deleteById(Long id) throws Exception {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new ClientNotFindException("ERROR : Client not found to DELETE");
        }
    }

    //GETTERS
    @Override
    public List<Client> getAll() throws Exception {
        return repository.findAll();
    }

    //SEARCHES
    @Override
    public Client searchById(Long id) throws Exception {
        Client client = repository.searchById(id);
        if (client == null) {
            throw new ClientNotFindException("ERROR : Client not found by id");
        }
        return client;
    }

    //SECONDARY METHODS
    private void validateClient(Client client) throws Exception {
        if (client == null) {
            throw new BadClientException("ERROR : Client is required");
        }
        if (client.getFullName() == null || client.getFullName().isBlank()) {
            throw new BadClientException("ERROR : Client full name is required");
        }
        if (client.getEmail() == null || client.getEmail().isBlank()) {
            throw new BadClientException("ERROR : Client email is required");
        }
        if (client.getVatCondition() == null) {
            throw new BadClientException("ERROR : Client vat condition is required");
        }
        if (client.getVatCondition() != VatCondition.CONSUMIDOR_FINAL) {
            if (client.getDocumentType() == null) {
                throw new BadClientException("ERROR : Client document type is required");
            }
            if (client.getDocumentNumber() == null || client.getDocumentNumber().isBlank()) {
                throw new BadClientException("ERROR : Client document number is required");
            }
            if (client.getCommercialAddress() == null || client.getCommercialAddress().isBlank()) {
                throw new BadClientException("ERROR : Client commercial address is required");
            }
        }
    }

    private void normalizeClient(Client client) {
        client.setFullName(normalize(client.getFullName()));
        client.setEmail(normalize(client.getEmail()));
        client.setCuit(normalize(client.getCuit()));
        client.setDocumentNumber(normalize(client.getDocumentNumber()));
        client.setCommercialAddress(normalize(client.getCommercialAddress()));
    }

    private String normalize(String value) {
        return value == null ? null : value.strip();
    }
}
