package com.sistema_contable.sistema.contable.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.Lot;
import com.sistema_contable.sistema.contable.model.accounting.Account;
import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.model.accounting.Entry;
import com.sistema_contable.sistema.contable.model.accounting.Movement;
import com.sistema_contable.sistema.contable.model.sales.Payment;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.AccountService;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.EntryService;

@Service
public class GoodsAccountingService {

  @Autowired
  private AccountService accountService;
  @Autowired
  private EntryService entryService;

  public void purchaseGoodsAccounting(List<Payment> payments, Product product, User user) throws Exception {
    Entry entry = new Entry();
    List<Movement> movements = new ArrayList<>();

    // Search the goods Account
    Account goodsData = accountService.searchByName("Mercaderías");
    BalanceAccount goods = accountService.searchBalanceAccount(goodsData.getId());

    // Create a movement for the goods account with the debit of the total cost of the goods
    Movement goodsMovement = new Movement();
    goodsMovement.setAccount(goods);

    // Set the credit of the movement with the total cost of the goods (unit price * stock)
    Double totalCost = product.getLots().stream().mapToDouble(lot -> lot.getUnitPrice() * lot.getStock()).sum();
    goodsMovement.setDebit(totalCost);
    goodsMovement.setCredit(0);
    movements.add(goodsMovement);

    // For each payment, create a movement with the debit in the corresponding account
    for (Payment payment : payments) {
      Movement paymentMovement = new Movement();
      paymentMovement.setAccount(payment.getPaymentType().getAccount());
      paymentMovement.setCredit(payment.getAmount());
      paymentMovement.setDebit(0);
      movements.add(paymentMovement);
    }

    entry.setDescription("Compra de mercaderías: " + product.getName());
    entry.setMovements(movements);

    entryService.create(entry, user);
  }

  public void purchaseLotAccounting(List<Payment> payments, Product product, Lot lot, User user) throws Exception {
    Entry entry = new Entry();
    List<Movement> movements = new ArrayList<>();

    Account goodsData = accountService.searchByName("Mercaderías");
    BalanceAccount goods = accountService.searchBalanceAccount(goodsData.getId());

    Movement goodsMovement = new Movement();
    goodsMovement.setAccount(goods);
    goodsMovement.setDebit(lot.getUnitPrice() * lot.getStock());
    goodsMovement.setCredit(0.0);
    movements.add(goodsMovement);

    for (Payment payment : payments) {
      Movement paymentMovement = new Movement();
      paymentMovement.setAccount(payment.getPaymentType().getAccount());
      paymentMovement.setCredit(payment.getAmount());
      paymentMovement.setDebit(0.0);
      movements.add(paymentMovement);
    }

    entry.setDescription("Compra de lote de mercaderías: " + product.getName());
    entry.setMovements(movements);

    entryService.create(entry, user);
  }
}
