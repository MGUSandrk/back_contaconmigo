package com.sistema_contable.sistema.contable.services.sales;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sistema_contable.sistema.contable.dto.sales.InvoiceItemResponseDTO;
import com.sistema_contable.sistema.contable.dto.sales.InvoiceResponseDTO;
import com.sistema_contable.sistema.contable.dto.sales.SaleItemRequestDTO;
import com.sistema_contable.sistema.contable.dto.sales.SaleItemResponseDTO;
import com.sistema_contable.sistema.contable.dto.sales.SalePaymentResponseDTO;
import com.sistema_contable.sistema.contable.dto.sales.SaleRequestDTO;
import com.sistema_contable.sistema.contable.dto.sales.SaleResponseDTO;
import com.sistema_contable.sistema.contable.exceptions.sales.BadSaleException;
import com.sistema_contable.sistema.contable.exceptions.sales.ClientNotFoundException;
import com.sistema_contable.sistema.contable.exceptions.sales.InsufficientStockException;
import com.sistema_contable.sistema.contable.model.CostingMethodType;
import com.sistema_contable.sistema.contable.model.EntityModel;
import com.sistema_contable.sistema.contable.model.Lot;
import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.model.accounting.Account;
import com.sistema_contable.sistema.contable.model.accounting.BalanceAccount;
import com.sistema_contable.sistema.contable.model.accounting.Entry;
import com.sistema_contable.sistema.contable.model.accounting.Movement;
import com.sistema_contable.sistema.contable.model.sales.Client;
import com.sistema_contable.sistema.contable.model.sales.Invoice;
import com.sistema_contable.sistema.contable.model.sales.InvoiceItem;
import com.sistema_contable.sistema.contable.model.sales.InvoiceType;
import com.sistema_contable.sistema.contable.model.sales.Payment;
import com.sistema_contable.sistema.contable.model.sales.Sale;
import com.sistema_contable.sistema.contable.model.sales.SaleProduct;
import com.sistema_contable.sistema.contable.repository.InvoiceRepository;
import com.sistema_contable.sistema.contable.repository.LotRepository;
import com.sistema_contable.sistema.contable.repository.PaymentTypeRepository;
import com.sistema_contable.sistema.contable.repository.ProductRepository;
import com.sistema_contable.sistema.contable.repository.SaleRepository;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.AccountService;
import com.sistema_contable.sistema.contable.services.accounting.interfaces.EntryService;
import com.sistema_contable.sistema.contable.services.interfaces.ClientService;
import com.sistema_contable.sistema.contable.services.interfaces.EntityService;
import com.sistema_contable.sistema.contable.services.interfaces.SaleService;

@Service
public class SaleServiceImp implements SaleService {

    //dependencies
    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private ClientService clientService;

    @Autowired
    private EntityService entityService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private PaymentTypeRepository paymentTypeRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private EntryService entryService;

    @Autowired
    private SalesReportPdfService salesReportPdfService;

    //CRUD
    @Override
    @Transactional
    public InvoiceResponseDTO createSale(SaleRequestDTO saleRequestDTO, User seller) throws Exception {
        // Validations
        Client client = clientService.searchById(saleRequestDTO.getClientId());
        EntityModel entity = entityService.getEntity();
        CostingMethodType costingMethod = entityService.getCostingMethod();

        // Calculate subtotal from items
        Double subtotal = 0.0;
        ListLotCost lotCosts = new ListLotCost();

        for (SaleItemRequestDTO item : saleRequestDTO.getItems()) {
            Product product = productRepository.searchById(item.getProductId());
            subtotal += product.getSalePrice() * item.getQuantity();

            // Validate stock
            Integer availableStock = lotRepository.findByProductWithStock(item.getProductId())
                    .stream()
                    .mapToInt(Lot::getStock)
                    .sum();

            if (availableStock < item.getQuantity()) {
                throw new InsufficientStockException(
                    "ERROR : Insufficient stock for product " + product.getName() + 
                    ". Available: " + availableStock + ", Requested: " + item.getQuantity()
                );
            }

            // Calculate cost using costing method
            Double itemCost = calculateItemCost(product, item.getQuantity(), costingMethod, lotCosts);
            lotCosts.addCost(itemCost);
        }

        // Apply discount
        Double discountAmount = (saleRequestDTO.getDiscount() != null) ? 
            (subtotal * saleRequestDTO.getDiscount() / 100) : 0.0;
        Double total = subtotal - discountAmount;

        // Create Sale
        Sale sale = new Sale();
        sale.setDateCreated(new Date());
        sale.setClient(client);
        sale.setSeller(seller);
        sale.setEntity(entity);
        sale.setTotalPrice(total);
        sale.setSaleProducts(new ArrayList<>());
        sale.setPayments(new ArrayList<>());

        // Create SaleProducts
        for (SaleItemRequestDTO item : saleRequestDTO.getItems()) {
            Product product = productRepository.searchById(item.getProductId());
            SaleProduct saleProduct = new SaleProduct();
            saleProduct.setProduct(product);
            saleProduct.setQuantity(item.getQuantity());
            saleProduct.setPrice(product.getSalePrice());
            sale.getSaleProducts().add(saleProduct);
        }

        // Create Payment
        Payment payment = new Payment();
        payment.setAmount(total);
        payment.setPaymentType(paymentTypeRepository.searchByName(saleRequestDTO.getPaymentMethod()));
        if (payment.getPaymentType() == null) {
            throw new RuntimeException("ERROR : Payment type not found: " + saleRequestDTO.getPaymentMethod());
        }
        sale.getPayments().add(payment);

        saleRepository.save(sale);

        // Deduct stock from lots
        deductStock(saleRequestDTO, costingMethod);

        // Create Invoice (immutable snapshot)
        InvoiceType invoiceType = InvoiceTypeResolver.resolve(entity.getVatCondition(), client.getVatCondition());
        Invoice invoice = createInvoice(sale, client, seller, entity, subtotal, discountAmount, total, 
                saleRequestDTO, lotCosts.getTotalCost(), invoiceType);
        invoiceRepository.save(invoice);

        // Create accounting entry for sale
        createSaleEntry(sale, seller, payment.getPaymentType().getAccount());

        // Create accounting entry for CMV
        createCMVEntry(sale, seller, lotCosts.getTotalCost());

        return mapToInvoiceResponseDTO(invoice);
    }

    //GETTERS
    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getAllSales() throws Exception {
        List<Sale> sales = saleRepository.findAll();
        return sales.stream()
                .map(this::mapToSaleResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getSalesByDate(Integer month, Integer year) throws Exception {
        validateSalesPeriod(month, year);
        Date startDate = getMonthStart(month, year);
        Date endDate = getNextMonthStart(startDate);
        return saleRepository.findByDateCreatedBetween(startDate, endDate)
                .stream()
                .map(this::mapToSaleResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponseDTO> getSalesByClientId(Long clientId) throws Exception {
        Client client = clientService.searchById(clientId);
        if (client == null) {
            throw new ClientNotFoundException("ERROR : Client not found with id: " + clientId);
        }
        List<Sale> sales = saleRepository.findByClientId(clientId);
        return sales.stream()
                .map(this::mapToSaleResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getInvoicesByClientCuit(String clientCuit) throws Exception {
        List<Invoice> invoices = invoiceRepository.findByClientCuit(clientCuit);
        return invoices.stream()
                .map(this::mapToInvoiceResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countSalesOfCurrentMonth() throws Exception {
        Date startDate = getCurrentMonthStart();
        Date endDate = getNextMonthStart(startDate);
        return saleRepository.countSalesByDateCreatedBetween(startDate, endDate);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generateMonthlySalesReportPdf(Integer month, Integer year) throws Exception {
        List<SaleResponseDTO> sales = getSalesByDate(month, year);
        Map<String, Double> totalsByPaymentType = totalsByPaymentType(sales);
        Double totalIncome = totalsByPaymentType.values()
                .stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        return salesReportPdfService.generarPdf(month, year, sales, totalsByPaymentType, totalIncome);
    }

    //SECONDARY METHODS
    private Date getCurrentMonthStart() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getNextMonthStart(Date startDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, 1);
        return calendar.getTime();
    }

    private Date getMonthStart(Integer month, Integer year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private void validateSalesPeriod(Integer month, Integer year) throws BadSaleException {
        if (month == null || month < 1 || month > 12) {
            throw new BadSaleException("ERROR : Sale month is invalid");
        }
        if (year == null || year <= 0) {
            throw new BadSaleException("ERROR : Sale year is invalid");
        }
    }

    private SaleResponseDTO mapToSaleResponseDTO(Sale sale) {
        SaleResponseDTO dto = new SaleResponseDTO();
        dto.setId(sale.getId());
        dto.setDateCreated(sale.getDateCreated());
        dto.setClientId(sale.getClient() != null ? sale.getClient().getId() : null);
        dto.setClientFullName(sale.getClient() != null ? sale.getClient().getFullName() : null);
        dto.setSellerId(sale.getSeller() != null ? sale.getSeller().getId() : null);
        dto.setSellerUsername(sale.getSeller() != null ? sale.getSeller().getUsername() : null);
        dto.setEntityId(sale.getEntity() != null ? sale.getEntity().getId() : null);
        dto.setEntityName(sale.getEntity() != null ? sale.getEntity().getName() : null);
        dto.setTotalPrice(sale.getTotalPrice());
        if (sale.getSaleProducts() != null) {
            for (SaleProduct saleProduct : sale.getSaleProducts()) {
                dto.getProducts().add(mapToSaleItemResponseDTO(saleProduct));
            }
        }
        if (sale.getPayments() != null) {
            for (Payment payment : sale.getPayments()) {
                dto.getPayments().add(mapToSalePaymentResponseDTO(payment));
            }
        }
        return dto;
    }

    private SaleItemResponseDTO mapToSaleItemResponseDTO(SaleProduct saleProduct) {
        SaleItemResponseDTO dto = new SaleItemResponseDTO();
        dto.setProductName(saleProduct.getProduct() != null ? saleProduct.getProduct().getName() : null);
        dto.setQuantity(saleProduct.getQuantity());
        return dto;
    }

    private SalePaymentResponseDTO mapToSalePaymentResponseDTO(Payment payment) {
        SalePaymentResponseDTO dto = new SalePaymentResponseDTO();
        dto.setMethod(payment.getPaymentType() != null ? payment.getPaymentType().getType() : null);
        dto.setAmount(payment.getAmount());
        return dto;
    }

    private Map<String, Double> totalsByPaymentType(List<SaleResponseDTO> sales) {
        Map<String, Double> totals = new LinkedHashMap<>();
        for (SaleResponseDTO sale : sales) {
            for (SalePaymentResponseDTO payment : sale.getPayments()) {
                String method = payment.getMethod() != null ? payment.getMethod() : "Sin metodo";
                Double amount = payment.getAmount() != null ? payment.getAmount() : 0.0;
                totals.put(method, totals.getOrDefault(method, 0.0) + amount);
            }
        }
        return totals;
    }

    private InvoiceResponseDTO mapToInvoiceResponseDTO(Invoice invoice) {
        InvoiceResponseDTO dto = new InvoiceResponseDTO();
        dto.setId(invoice.getId());
        dto.setInvoiceNumber(invoice.getInvoiceNumber());
        dto.setInvoiceType(invoice.getInvoiceType());
        dto.setDateCreated(invoice.getDateCreated());
        dto.setClientFullName(invoice.getClientFullName());
        dto.setClientCuit(invoice.getClientCuit());
        dto.setSellerFullName(invoice.getSellerFullName());
        dto.setEntityName(invoice.getEntityName());
        dto.setEntityCuit(invoice.getEntityCuit());
        dto.setEntityCommercialAddress(invoice.getEntityCommercialAddress());
        dto.setEntityGrossIncomeNumber(invoice.getEntityGrossIncomeNumber());
        dto.setEntityVatCondition(invoice.getEntityVatCondition());
        dto.setEntityActivityStartDate(invoice.getEntityActivityStartDate());
        dto.setSalesPoint(invoice.getSalesPoint());
        dto.setSubtotal(invoice.getSubtotal());
        dto.setDiscountAmount(invoice.getDiscountAmount());
        dto.setTotal(invoice.getTotal());
        dto.setPaymentMethod(invoice.getPaymentMethod());
        dto.setInstallments(invoice.getInstallments());
        dto.setCostingMethod(invoice.getCostingMethod());
        dto.setCmvAmount(invoice.getCmvAmount());
        dto.setLegalInvoiceNumber(invoice.getLegalInvoiceNumber());
        dto.setCae(invoice.getCae());
        dto.setCaeExpirationDate(invoice.getCaeExpirationDate());
        dto.setQrCodeBase64(invoice.getQrCodeBase64());
        if (invoice.getItems() != null) {
            dto.setItems(invoice.getItems().stream().map(this::mapToInvoiceItemResponseDTO).toList());
        }
        return dto;
    }

    private InvoiceItemResponseDTO mapToInvoiceItemResponseDTO(InvoiceItem invoiceItem) {
        InvoiceItemResponseDTO dto = new InvoiceItemResponseDTO();
        dto.setId(invoiceItem.getId());
        dto.setProductId(invoiceItem.getProductId());
        dto.setProductName(invoiceItem.getProductName());
        dto.setQuantity(invoiceItem.getQuantity());
        dto.setUnitPrice(invoiceItem.getUnitPrice());
        dto.setSubtotal(invoiceItem.getSubtotal());
        return dto;
    }

    private Double calculateItemCost(Product product, Integer quantity, CostingMethodType costingMethod,
            ListLotCost lotCosts) throws Exception {
        List<Lot> lots;
        
        switch (costingMethod) {
            case FIFO:
                lots = lotRepository.findByProductWithStockFIFO(product.getId());
                break;
            case LIFO:
                lots = lotRepository.findByProductWithStockLIFO(product.getId());
                break;
            case WAC:
                lots = lotRepository.findByProductWithStock(product.getId());
                break;
            default:
                lots = lotRepository.findByProductWithStockFIFO(product.getId());
        }

        Double totalCost = 0.0;
        Integer remaining = quantity;

        for (Lot lot : lots) {
            if (remaining <= 0) break;

            Integer toTake = Math.min(remaining, lot.getStock());
            totalCost += toTake * lot.getUnitPrice();
            remaining -= toTake;
        }

        return totalCost;
    }

    private void deductStock(SaleRequestDTO saleRequestDTO, CostingMethodType costingMethod) throws Exception {
        for (SaleItemRequestDTO item : saleRequestDTO.getItems()) {
            List<Lot> lots;
            
            switch (costingMethod) {
                case FIFO:
                    lots = lotRepository.findByProductWithStockFIFO(item.getProductId());
                    break;
                case LIFO:
                    lots = lotRepository.findByProductWithStockLIFO(item.getProductId());
                    break;
                case WAC:
                    lots = lotRepository.findByProductWithStock(item.getProductId());
                    break;
                default:
                    lots = lotRepository.findByProductWithStockFIFO(item.getProductId());
            }

            Integer remaining = item.getQuantity();

            for (Lot lot : lots) {
                if (remaining <= 0) break;

                Integer toTake = Math.min(remaining, lot.getStock());
                lot.setStock(lot.getStock() - toTake);
                lotRepository.save(lot);
                remaining -= toTake;
            }
        }
    }

    private Invoice createInvoice(Sale sale, Client client, User seller, EntityModel entity, 
            Double subtotal, Double discountAmount, Double total, SaleRequestDTO saleRequestDTO, 
            Double cmvAmount, InvoiceType invoiceType) {
        return Invoice.fromSale(
                sale,
                client,
                seller,
                entity,
                invoiceType,
                saleRequestDTO.getPaymentMethod(),
                saleRequestDTO.getInstallments(),
                subtotal,
                discountAmount,
                total,
                cmvAmount);
    }

    private void createSaleEntry(Sale sale, User seller, BalanceAccount paymentAccount) throws Exception {
        Entry entry = new Entry();
        entry.setDescription("Venta #" + sale.getId() + " - " + sale.getClient().getFullName());
        
        List<Movement> movements = new ArrayList<>();

        // Debit: Payment account (Caja, Banco, etc.)
        Movement debitMovement = new Movement();
        debitMovement.setAccount(paymentAccount);
        debitMovement.setDebit(sale.getTotalPrice());
        debitMovement.setCredit(0.0);
        movements.add(debitMovement);

        // Credit: Sales account
        Account salesData = accountService.searchByName("Ventas");
        if (salesData == null) {
            throw new RuntimeException("ERROR : Sales account not found");
        }
        BalanceAccount salesAccount = accountService.searchBalanceAccount(salesData.getId());
        
        Movement creditMovement = new Movement();
        creditMovement.setAccount(salesAccount);
        creditMovement.setDebit(0.0);
        creditMovement.setCredit(sale.getTotalPrice());
        movements.add(creditMovement);

        entry.setMovements(movements);
        entryService.create(entry, seller);
    }

    private void createCMVEntry(Sale sale, User seller, Double cmvAmount) throws Exception {
        Entry entry = new Entry();
        entry.setDescription("CMV por venta #" + sale.getId());
        
        List<Movement> movements = new ArrayList<>();

        // Debit: CMV account (Resultado Negativo) TODO Revisar el nombre que se le va a poner a la cuenta de costo de mercaderías vendidas
        Account expenseData = accountService.searchByName("Costo de Mercaderías Vendidas");
        if (expenseData == null) {
            throw new RuntimeException("ERROR : Expense account not found");
        }
        BalanceAccount expenseAccount = accountService.searchBalanceAccount(expenseData.getId());
        
        Movement debitMovement = new Movement();
        debitMovement.setAccount(expenseAccount);
        debitMovement.setDebit(cmvAmount);
        debitMovement.setCredit(0.0);
        movements.add(debitMovement);

        // Credit: Merchandise account
        Account goodsData = accountService.searchByName("Mercaderías");
        if (goodsData == null) {
            throw new RuntimeException("ERROR : Merchandise account not found");
        }
        BalanceAccount goodsAccount = accountService.searchBalanceAccount(goodsData.getId());
        
        Movement creditMovement = new Movement();
        creditMovement.setAccount(goodsAccount);
        creditMovement.setDebit(0.0);
        creditMovement.setCredit(cmvAmount);
        movements.add(creditMovement);

        entry.setMovements(movements);
        entryService.create(entry, seller);
    }

    // Helper class to track total cost
    private static class ListLotCost {
        private Double totalCost = 0.0;

        public void addCost(Double cost) {
            this.totalCost += cost;
        }

        public Double getTotalCost() {
            return totalCost;
        }
    }
}
