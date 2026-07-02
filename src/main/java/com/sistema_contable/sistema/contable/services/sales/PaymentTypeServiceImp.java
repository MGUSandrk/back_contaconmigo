package com.sistema_contable.sistema.contable.services.sales;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sistema_contable.sistema.contable.exceptions.sales.BadPaymentTypeException;
import com.sistema_contable.sistema.contable.exceptions.sales.PaymentTypeNotFindException;
import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.model.sales.PaymentType;
import com.sistema_contable.sistema.contable.repository.AccountRepository;
import com.sistema_contable.sistema.contable.repository.PaymentTypeRepository;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.AccountService;
import com.sistema_contable.sistema.contable.services.interfaces.PaymentTypeService;

@Service
public class PaymentTypeServiceImp implements PaymentTypeService {

    //dependencies
    @Autowired
    private PaymentTypeRepository repository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private AccountService accountService;

    //CRUD
    @Override
    public void create(PaymentType paymentType) throws Exception {
        this.validatePaymentType(paymentType);
        paymentType.setType(paymentType.getType().strip());
        paymentType.setAccount(this.searchAccount(paymentType.getAccount().getId()));
        repository.save(paymentType);
    }

    @Override
    public void modifyById(Long id, PaymentType paymentType) throws Exception {
        this.validatePaymentType(paymentType);
        PaymentType storedPaymentType = this.searchById(id);
        storedPaymentType.setType(paymentType.getType().strip());
        storedPaymentType.setAccount(this.searchAccount(paymentType.getAccount().getId()));
        repository.save(storedPaymentType);
    }

    @Override
    public void deleteById(Long id) throws Exception {
        if (repository.existsById(id)) {
            repository.deleteById(id);
        } else {
            throw new PaymentTypeNotFindException("ERROR : Payment type not found to DELETE");
        }
    }

    //GETTERS
    @Override
    public List<PaymentType> getAll() throws Exception {
        return repository.findAll();
    }

    //SEARCHES
    @Override
    public PaymentType searchById(Long id) throws Exception {
        PaymentType paymentType = repository.searchById(id);
        if (paymentType == null) {
            throw new PaymentTypeNotFindException("ERROR : Payment type not found by id");
        }
        return paymentType;
    }

    @Override
    public Double currentBalance(PaymentType paymentType) throws Exception {
        if (paymentType == null || paymentType.getAccount() == null || paymentType.getAccount().getId() == null) {
            return 0D;
        }
        return accountService.lastBalance(paymentType.getAccount().getId());
    }

    //SECONDARY METHODS
    private void validatePaymentType(PaymentType paymentType) throws Exception {
        if (paymentType == null) {
            throw new BadPaymentTypeException("ERROR : Payment type is required");
        }
        if (paymentType.getType() == null || paymentType.getType().isBlank()) {
            throw new BadPaymentTypeException("ERROR : Payment type name is required");
        }
        if (paymentType.getAccount() == null || paymentType.getAccount().getId() == null) {
            throw new BadPaymentTypeException("ERROR : Payment type account is required");
        }
    }

    private BalanceAccount searchAccount(Long id) throws Exception {
        BalanceAccount account = accountRepository.searchBalanceAccount(id);
        if (account == null) {
            throw new BadPaymentTypeException("ERROR : Payment type account not found");
        }
        return account;
    }
}
