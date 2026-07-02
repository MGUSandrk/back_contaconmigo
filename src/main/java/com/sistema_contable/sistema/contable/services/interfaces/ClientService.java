package com.sistema_contable.sistema.contable.services.interfaces;

import java.util.List;

import com.sistema_contable.sistema.contable.model.sales.Client;

public interface ClientService {
    void create(Client client) throws Exception;
    List<Client> getAll() throws Exception;
    Client searchById(Long id) throws Exception;
    void modifyById(Long id, Client client) throws Exception;
    void deleteById(Long id) throws Exception;
}
