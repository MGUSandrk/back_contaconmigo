package com.sistema_contable.sistema.contable.services.accounting;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sistema_contable.sistema.contable.exceptions.accounting.AccountNotActiveException;
import com.sistema_contable.sistema.contable.exceptions.accounting.AccountNotFindException;
import com.sistema_contable.sistema.contable.exceptions.accounting.NotEnoughBalanceException;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.model.accounting.Entry;
import com.sistema_contable.sistema.contable.model.accounting.Movement;
import com.sistema_contable.sistema.contable.repository.EntryRepository;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.AccountService;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.EntryService;

@Service
public class EntryServiceImp implements EntryService {

    //dependencies
    @Autowired
    private EntryRepository repository;
    @Autowired
    private AccountService accountService;


    //CRUD
    @Override
    public void create(Entry entry, User userDB)throws Exception{
        entry.setDateCreated(new Date());
        entry.setUserCreator(userDB);
        this.configMovements(entry);
        repository.save(entry);}

    //SECONDARY METHODS
    private void configMovements(Entry entry)throws Exception{
        for (Movement movement : entry.getMovements()){
            BalanceAccount account = accountService.searchBalanceAccount(movement.getAccount().getId());
            if(account==null){
                throw new AccountNotFindException("ERROR : Account not find to set to movements");}
            else{
                //check the state of account
                if (!account.isActive()){throw new AccountNotActiveException("ERROR : Account not active ot set to movements");}
                movement.setEntry(entry);
                movement.setAccount(account);
                //check the balance of the account
                if (!movement.balanceEnough(accountService.lastBalance(account.getId()))){
                    throw new NotEnoughBalanceException("ERROR : ccount not enough balance to use in movements");}
                movement.addAccountBalance(accountService.lastBalance(account.getId()));}}}
}
