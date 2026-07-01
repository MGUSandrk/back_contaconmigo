# Invoice Fiscal Simulation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add educational fiscal-simulation fields to invoices and render them in a more realistic Argentine invoice PDF.

**Architecture:** `Invoice.fromSale(...)` remains the creation point for the immutable invoice snapshot. New simulated fiscal values are stored on `Invoice`, exposed in `InvoiceResponseDTO`, copied in `SaleServiceImp`, and rendered by the existing Thymeleaf/OpenHTMLtoPDF flow.

**Tech Stack:** Java 17, Spring Boot, JPA, Thymeleaf, OpenHTMLtoPDF, JUnit 5, Maven.

---

### Task 1: Add Simulated Fiscal Data To Invoice

**Files:**
- Modify: `src/test/java/com/sistema_contable/sistema/contable/model/sales/InvoiceTest.java`
- Modify: `src/main/java/com/sistema_contable/sistema/contable/model/sales/Invoice.java`

- [ ] **Step 1: Write the failing test**

Add assertions to `fromSaleStoresInvoiceTypeEntitySnapshotAndItems`:

```java
assertEquals("00004-00000003", invoice.getLegalInvoiceNumber());
assertEquals("70417054367476", invoice.getCae());
assertEquals("data:image/png;base64,", invoice.getQrCodeBase64().substring(0, 22));
assertTrue(invoice.getCaeExpirationDate().after(invoice.getDateCreated()));
```

- [ ] **Step 2: Run the focused test and verify it fails**

Run: `./mvnw -Dtest=InvoiceTest test`
Expected: compilation failure or assertion failure because the new getters do not exist yet.

- [ ] **Step 3: Implement invoice fields and generation**

Add fields, getters, setters, and helper methods in `Invoice`:

```java
private static final String SIMULATED_CAE = "70417054367476";
private static final String SIMULATED_QR_BASE64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAGQAAABkAQAAAABYmaj5AAAAJElEQVR4XmNgGAWjYBSMglEwCkbBKBgFo2AUjIJRMApGwagAACqAAAGWdRkwAAAAAElFTkSuQmCC";
private String legalInvoiceNumber;
private String cae;
private Date caeExpirationDate;
private String qrCodeBase64;
```

Set them from `fromSale(...)` using sale id, sales point, date + 10 days, and constants.

- [ ] **Step 4: Run the focused test and verify it passes**

Run: `./mvnw -Dtest=InvoiceTest test`
Expected: test passes.

### Task 2: Expose Fiscal Data In DTO Mapping

**Files:**
- Modify: `src/main/java/com/sistema_contable/sistema/contable/dto/InvoiceResponseDTO.java`
- Modify: `src/main/java/com/sistema_contable/sistema/contable/services/sales/SaleServiceImp.java`

- [ ] **Step 1: Add DTO fields**

Add `legalInvoiceNumber`, `cae`, `caeExpirationDate`, and `qrCodeBase64` with getters and setters.

- [ ] **Step 2: Map invoice fields**

In `SaleServiceImp.mapToInvoiceResponseDTO(...)`, copy the new values from `Invoice` to `InvoiceResponseDTO`.

- [ ] **Step 3: Compile focused tests**

Run: `./mvnw -Dtest=InvoiceTest,SaleServiceImpTest test`
Expected: tests pass or reveal any compile errors from the new fields.

### Task 3: Render The Legal-Similar PDF

**Files:**
- Modify: `src/main/resources/templates/pdf/factura.html`

- [ ] **Step 1: Replace the basic PDF layout**

Use the existing `factura` object and render:

```html
<div class="type-box"><span th:text="${factura.invoiceType}">B</span></div>
<div th:text="${factura.legalInvoiceNumber}">00004-00000003</div>
<img th:src="${factura.qrCodeBase64}" alt="QR" />
```

Include issuer data, client data, items, totals, CAE, CAE expiration, and educational legend.

- [ ] **Step 2: Run service tests**

Run: `./mvnw -Dtest=InvoiceTest,InvoiceServiceImpTest,SaleServiceImpTest test`
Expected: tests pass.

### Task 4: Verify Build

**Files:**
- No code files.

- [ ] **Step 1: Run full test suite**

Run: `./mvnw test`
Expected: all tests pass.
