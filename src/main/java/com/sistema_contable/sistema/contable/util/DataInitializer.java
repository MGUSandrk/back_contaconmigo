package com.sistema_contable.sistema.contable.util;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.sistema_contable.sistema.contable.model.CostingMethodType;
import com.sistema_contable.sistema.contable.model.EntityModel;
import com.sistema_contable.sistema.contable.model.Role;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.VatCondition;
import com.sistema_contable.sistema.contable.model.accounting.Account;
import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.model.accounting.ControlAccount;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.AccountService;
import com.sistema_contable.sistema.contable.services.interfaces.EntityService;
import com.sistema_contable.sistema.contable.services.interfaces.UserService;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private EntityService entityService;

    public DataInitializer(UserService userService, AccountService accountService, EntityService entityService) {
        this.userService = userService;
        this.accountService = accountService;
        this.entityService = entityService;
    }

    @Override
    public void run(String... args) throws Exception {
        //if accounts table are empty add the basic set of account
        if (accountService.getAll().isEmpty()){
            addAccounts();
        }
        //if users table are empty add the admin
        if (userService.getAll().isEmpty()){
            addUsers();
        }
    }

    private void addUsers() throws Exception {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin");
        admin.setRole(Role.ADMIN);
        userService.create(admin);
    }

    private void addAccounts() throws Exception{
        Account asset = new ControlAccount();
        asset.setName("Activo");
        asset.setPlus(true);
        accountService.create(asset,null);

        Account pasivo = new ControlAccount();
        pasivo.setName("Pasivo");
        pasivo.setPlus(false);
        accountService.create(pasivo,null);

        Account patrimonio = new ControlAccount();
        patrimonio.setName("Patrimonio");
        patrimonio.setPlus(false);
        accountService.create(patrimonio,null);

        Account ingresos = new ControlAccount();
        ingresos.setName("Resultado Positivo");
        ingresos.setPlus(false);
        accountService.create(ingresos,null);

        Account egreso = new ControlAccount();
        egreso.setName("Resultado Negativo");
        egreso.setPlus(true);
        accountService.create(egreso,null);

        Account cajaybanco = new ControlAccount();
        cajaybanco.setName("Caja y Banco");
        accountService.create(cajaybanco, accountService.searchByName("Activo").getId());

        Account caja= new BalanceAccount();
        caja.setName("Caja");
        accountService.create(caja, accountService.searchByName("Caja y Banco").getId());

        Account documentosACobrar = new BalanceAccount();
        documentosACobrar.setName("Documentos a cobrar");
        accountService.create(documentosACobrar, accountService.searchByName("Caja y Banco").getId());

        Account valoresADepositar = new BalanceAccount();
        valoresADepositar.setName("Valores a depositar");
        accountService.create(valoresADepositar, accountService.searchByName("Caja y Banco").getId());

        Account creditos  = new ControlAccount();
        creditos.setName("Crédito");
        accountService.create(creditos, accountService.searchByName("Activo").getId());

        Account bienes  = new ControlAccount();
        bienes.setName("Bienes de cambio");
        accountService.create(bienes, accountService.searchByName("Activo").getId());

        Account mercaderias  = new BalanceAccount();
        mercaderias.setName("Mercaderías");
        accountService.create(mercaderias, accountService.searchByName("Bienes de cambio").getId());

        Account deudascom = new ControlAccount();
        deudascom.setName("Deudas de comerciales");
        accountService.create(deudascom, accountService.searchByName("Pasivo").getId());

        Account capital = new BalanceAccount();
        capital.setName("Capital");
        accountService.create(capital, accountService.searchByName("Patrimonio").getId());

        Account ventas = new BalanceAccount();
        ventas.setName("Ventas");
        accountService.create(ventas, accountService.searchByName("Resultado Positivo").getId());

        Account otrosing = new ControlAccount();
        otrosing.setName("Otros ingresos");
        accountService.create(otrosing, accountService.searchByName("Resultado Positivo").getId());

        Account costoMercaderiasVendidas = new BalanceAccount();
        costoMercaderiasVendidas.setName("Costo de Mercaderías Vendidas");
        accountService.create(costoMercaderiasVendidas, accountService.searchByName("Resultado Negativo").getId());

        Account descuentosOtorgados = new BalanceAccount();
        descuentosOtorgados.setName("Descuentos Otorgados");
        accountService.create(descuentosOtorgados, accountService.searchByName("Resultado Negativo").getId());

        EntityModel entity = new EntityModel();
        entity.setName("Empresa");
        entity.setCostingMethod(CostingMethodType.FIFO);
        entity.setCuit("12345678901");
        entity.setCommercialAddress("Esmeralda 123");
        entity.setGrossIncomeNumber("NO CONTRIBUYENTE");
        entity.setVatCondition(VatCondition.IVA_RESPONSABLE_INSCRIPTO);
        entity.setActivityStartDate(new Date());
        entity.setSalesPoint(1);
        entityService.create(entity);
    }
}
