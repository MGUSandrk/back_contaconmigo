package com.sistema_contable.sistema.contable.services.accounting;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sistema_contable.sistema.contable.exceptions.accounting.AccountNotFindException;
import com.sistema_contable.sistema.contable.exceptions.accounting.BadAccountException;
import com.sistema_contable.sistema.contable.model.accounting.Account;
import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.model.accounting.ControlAccount;
import com.sistema_contable.sistema.contable.repository.AccountRepository;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.AccountService;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.MovementService;

@Service
public class AccountServiceImp implements AccountService {

    @Autowired
    private AccountRepository repository;
    @Autowired
    private MovementService movementService;

    //CRUD
    @Override
    public void create(Account account, Long accountID)throws Exception{
        //formats the name
        String name = account.getName().strip();
        String formatName = name.substring(0, 1).toUpperCase() + name.substring(1);
        account.setName(formatName);
        //set the state off the account in true/active
        account.setActive(true);
        //check that there are no accounts with the same name
        if(this.searchByName(account.getName())!=null) {throw new AccountNotFindException("ERROR : Found account with same name");}
        //new account with father
        if(accountID!=null){
            //search in db the "father" account
            ControlAccount accountBD = this.searchControlAccount(accountID);
            if(accountBD==null){throw new AccountNotFindException("ERROR : Father account not found");}
            accountBD.addChildren(account);
            account.setPlus(accountBD.isPlus());
            repository.save(accountBD);}
        //new root account
        else{
            this.rootCode(account);
            repository.save(account);}}

    //delete
    @Override
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public void delete(Long id)throws Exception{
        if(this.existAccountById(id)){
            Account account = this.searchById(id);
            if(this.accountUsedInMovements(account) || !account.getSubAccounts().isEmpty()){
                //logic delete
                account.desactivate();
                repository.save(account);}
            else{//physical delete
                repository.deleteAccountById(id);}}
        else {throw new AccountNotFindException("ERROR : Account not found to DELETE");}}

    //activate
    @Override
    public void activate(Long id)throws Exception{
        if(this.existAccountById(id)){
            Account account = this.searchById(id);
            account.activate();
            repository.save(account);}
        else{throw new AccountNotFindException("ERROR : Account not found to ACTIVATE");}}

    //update the name of the account
    @Override
    public void update(Long id, String nombre) throws Exception {
        if(this.searchByName(nombre)!=null){throw new BadAccountException("ERROR : Account not found to UPDATE");}
        Account account = this.searchById(id);
        account.setName(nombre);
        repository.save(account);}


    //GETTERS
    //get all
    @Override
    public List<Account> getAll() throws Exception {return repository.findAll();}

    //get all balance accounts
    @Override
    public List<BalanceAccount> getBalanceAccounts() throws Exception {return repository.getBalanceAccounts();}

    //get all control accounts
    @Override
    public List<ControlAccount> getRootAccounts()throws Exception{return repository.getRootAccounts();}

    //search last balance of account
    @Override
    public Double lastBalance(Long id) throws Exception {
        Account account = this.searchById(id);
        if(account.getSubAccounts().isEmpty()){
            return this.searchLastBalance(id);}
        else{
            Double total = 0D;
            for(Account child : account.getSubAccounts()){
                total +=  this.lastBalance(child.getId());}
            return total;}}

    //calculate the results
    @Override
    public Double results() throws Exception {
        return lastBalance(4L)-lastBalance(5L);
    }

    //calculate the equity
    @Override
    public Double equity() throws Exception {
        return lastBalance(1L)-lastBalance(2L);
    }


    //SEARCHES
    //by id all types
    @Override
    public Account searchById(Long id) throws Exception{
        Account account = repository.searchById(id);
        if(account==null){throw new AccountNotFindException("ERROR : Account not found by id");}
        else{return account;}}

    //by name
    @Override
    public Account searchByName(String name) throws Exception{return repository.searchByName(name);}

    //balance accounts by id
    @Override
    public BalanceAccount searchBalanceAccount(Long id) throws Exception {return repository.searchBalanceAccount(id);}

    //control accounts by id
    @Override
    public ControlAccount searchControlAccount(Long id) throws Exception {return repository.searchControlAccount(id);}


    //SECONDARY METHODS
    //searchBalance
    private Double searchLastBalance(Long id){
        Double lastBalance = repository.searchLastBalance(id);
        if(lastBalance==null){return 0D;}
        return lastBalance;
    }

    //logic for root accounts code
    private void rootCode(Account account) throws Exception {
        int accountCode = this.getRootAccounts().size()+1;
        account.setCode(String.valueOf(accountCode));}

    //check if the account is used in entrys/movements
    private boolean accountUsedInMovements(Account account) throws Exception {
        return movementService.existMovementByAccount(account);}

    //check if the account exists
    private boolean existAccountById(Long id){return repository.existsById(id);}
}
