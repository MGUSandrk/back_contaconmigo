package com.sistema_contable.sistema.contable.services.accounting;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.repository.AccountRepository;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.MovementService;

@ExtendWith(MockitoExtension.class)
class AccountServiceImpTest {

    @Mock
    private AccountRepository repository;

    @Mock
    private MovementService movementService;

    @InjectMocks
    private AccountServiceImp service;

    @Test
    void isSalesOrPurchaseAccountReturnsTrueForSalesAccount() throws Exception {
        BalanceAccount account = account("Ventas");
        when(repository.searchById(1L)).thenReturn(account);

        Boolean result = service.isSalesOrPurchaseAccount(1L);

        assertTrue(result);
        verify(repository).searchById(1L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void isSalesOrPurchaseAccountReturnsTrueForGoodsAccount() throws Exception {
        BalanceAccount account = account("Mercaderías");
        when(repository.searchById(2L)).thenReturn(account);

        Boolean result = service.isSalesOrPurchaseAccount(2L);

        assertTrue(result);
        verify(repository).searchById(2L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void isSalesOrPurchaseAccountReturnsTrueForCMVAccount() throws Exception {
        BalanceAccount account = account("Costo de Mercaderías Vendidas");
        when(repository.searchById(3L)).thenReturn(account);

        Boolean result = service.isSalesOrPurchaseAccount(3L);

        assertTrue(result);
        verify(repository).searchById(3L);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void isSalesOrPurchaseAccountReturnsFalseForRegularAccount() throws Exception {
        BalanceAccount account = account("Caja");
        when(repository.searchById(4L)).thenReturn(account);

        Boolean result = service.isSalesOrPurchaseAccount(4L);

        assertFalse(result);
        verify(repository).searchById(4L);
        verifyNoMoreInteractions(repository);
    }

    private BalanceAccount account(String name) {
        BalanceAccount account = new BalanceAccount();
        account.setName(name);
        return account;
    }
}
