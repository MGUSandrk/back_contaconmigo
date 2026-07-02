package com.sistema_contable.sistema.contable.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.sistema_contable.sistema.contable.model.accounting.Account;
import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.AccountService;
import com.sistema_contable.sistema.contable.services.interfaces.EntityService;
import com.sistema_contable.sistema.contable.services.interfaces.UserService;

class DataInitializerTest {

    @Test
    void runCreatesSalesAndCostOfGoodsSoldBalanceAccounts() throws Exception {
        UserService userService = mock(UserService.class);
        AccountService accountService = mock(AccountService.class);
        EntityService entityService = mock(EntityService.class);
        DataInitializer initializer = new DataInitializer(userService, accountService, entityService);

        when(userService.getAll()).thenReturn(Collections.emptyList());
        when(accountService.getAll()).thenReturn(Collections.emptyList());
        when(accountService.searchByName(any())).thenReturn(null);

        initializer.run();

        ArgumentCaptor<Account> accountCaptor = ArgumentCaptor.forClass(Account.class);
        ArgumentCaptor<Long> parentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(accountService, atLeastOnce()).create(accountCaptor.capture(), parentCaptor.capture());

        boolean createdSales = false;
        boolean createdCostOfGoodsSold = false;
        for (int i = 0; i < accountCaptor.getAllValues().size(); i++) {
            Account account = accountCaptor.getAllValues().get(i);
            Long parentId = parentCaptor.getAllValues().get(i);
            if (account instanceof BalanceAccount
                    && "Ventas".equals(account.getName())
                    && Long.valueOf(4L).equals(parentId)) {
                createdSales = true;
            }
            if (account instanceof BalanceAccount
                    && "Costo de Mercaderías Vendidas".equals(account.getName())
                    && Long.valueOf(5L).equals(parentId)) {
                createdCostOfGoodsSold = true;
            }
        }

        assertTrue(createdSales);
        assertTrue(createdCostOfGoodsSold);
        verify(accountService).create(any(Account.class), isNull());
    }
}
