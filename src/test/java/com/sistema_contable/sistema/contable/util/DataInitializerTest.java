package com.sistema_contable.sistema.contable.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sistema_contable.sistema.contable.model.CostingMethodType;
import com.sistema_contable.sistema.contable.model.EntityModel;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.accounting.Account;
import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.model.accounting.ControlAccount;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.AccountService;
import com.sistema_contable.sistema.contable.services.interfaces.EntityService;
import com.sistema_contable.sistema.contable.services.interfaces.UserService;

public class DataInitializerTest {

    @Test
    void runDoesNotRetrySeedWhenAccountsFailPartially() throws Exception {
        FakeUserService userService = new FakeUserService();
        FakeAccountService accountService = new FakeAccountService();
        FakeEntityService entityService = new FakeEntityService();
        DataInitializer dataInitializer = new DataInitializer(userService, accountService, entityService);

        assertThrows(IllegalStateException.class, () -> dataInitializer.run());

        org.junit.jupiter.api.Assertions.assertEquals(List.of("Activo", "Pasivo"), accountService.createdNames);
        org.junit.jupiter.api.Assertions.assertEquals(0, userService.createdUsers);
        org.junit.jupiter.api.Assertions.assertEquals(0, entityService.createdEntities);
    }

    private static class FakeAccountService implements AccountService {
        private final List<String> createdNames = new ArrayList<>();

        @Override
        public void create(Account account, Long accountID) throws Exception {
            createdNames.add(account.getName());
            if ("Pasivo".equals(account.getName())) {
                throw new IllegalStateException("account seed failed");
            }
        }

        @Override
        public List<Account> getAll() throws Exception {
            return Collections.emptyList();
        }

        @Override public void delete(Long id) throws Exception {}
        @Override public void update(Long id, String nombre) throws Exception {}
        @Override public List<BalanceAccount> getBalanceAccounts() throws Exception { return Collections.emptyList(); }
        @Override public List<ControlAccount> getRootAccounts() throws Exception { return Collections.emptyList(); }
        @Override public Account searchById(Long id) throws Exception { return null; }
        @Override public Account searchByName(String name) throws Exception { return null; }
        @Override public BalanceAccount searchBalanceAccount(Long id) throws Exception { return null; }
        @Override public ControlAccount searchControlAccount(Long id) throws Exception { return null; }
        @Override public Double lastBalance(Long id) throws Exception { return 0D; }
        @Override public Boolean isSalesOrPurchaseAccount(Long id) throws Exception { return false; }
        @Override public Double results() throws Exception { return 0D; }
        @Override public Double equity() throws Exception { return 0D; }
        @Override public void activate(Long id) throws Exception {}
    }

    private static class FakeUserService implements UserService {
        private int createdUsers = 0;

        @Override
        public void create(User user) throws Exception {
            createdUsers++;
        }

        @Override public User findByUsername(String username) throws Exception { return null; }
        @Override public List<User> getAll() throws Exception { return Collections.emptyList(); }
        @Override public void delete(Long id) throws Exception {}
    }

    private static class FakeEntityService implements EntityService {
        private int createdEntities = 0;

        @Override
        public void create(EntityModel entity) throws Exception {
            createdEntities++;
        }

        @Override public EntityModel getEntity() throws Exception { return null; }
        @Override public CostingMethodType getCostingMethod() throws Exception { return null; }
        @Override public void modify(EntityModel entity) throws Exception {}
    }
}
