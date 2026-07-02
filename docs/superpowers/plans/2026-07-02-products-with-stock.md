# Products With Stock Endpoint Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add `GET /products/with-stock`, returning the same product response DTOs as `GET /products` but only for products whose total lot stock is greater than zero.

**Architecture:** Keep the existing layered flow. `ProductResource` authorizes and maps responses, `ProductService` exposes the business operation, and `ProductRepository` performs the stock filter with JPQL over `Product` and `Lot`.

**Tech Stack:** Java 17, Spring Boot 3.5.5, Spring Data JPA, JUnit 5, Mockito, Maven.

---

## File Structure

- Modify `src/main/java/com/sistema_contable/sistema/contable/repository/ProductRepository.java`
  - Add `findAllWithStock()` query.
- Modify `src/main/java/com/sistema_contable/sistema/contable/services/interfaces/ProductService.java`
  - Add `getAllWithStock()` contract.
- Modify `src/main/java/com/sistema_contable/sistema/contable/services/ProductServiceImp.java`
  - Implement `getAllWithStock()` by delegating to the repository.
- Modify `src/main/java/com/sistema_contable/sistema/contable/resources/ProductResource.java`
  - Add `GET /products/with-stock` with seller authorization and existing response mapping.
- Create `src/test/java/com/sistema_contable/sistema/contable/services/ProductServiceImpTest.java`
  - Cover service delegation.
- Create `src/test/java/com/sistema_contable/sistema/contable/resources/ProductResourceTest.java`
  - Cover authorization, service call, status, and DTO response shape.

Existing uncommitted changes are present in the repository. Only stage files touched by this plan when committing.

---

### Task 1: Add Failing Service Test

**Files:**
- Create: `src/test/java/com/sistema_contable/sistema/contable/services/ProductServiceImpTest.java`

- [ ] **Step 1: Write the failing service test**

Create `src/test/java/com/sistema_contable/sistema/contable/services/ProductServiceImpTest.java`:

```java
package com.sistema_contable.sistema.contable.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceImpTest {

    @Mock
    private ProductRepository repository;

    @Mock
    private GoodsAccountingService goodsAccountingService;

    @InjectMocks
    private ProductServiceImp service;

    @Test
    void getAllWithStockReturnsRepositoryProducts() throws Exception {
        Product product = new Product();
        product.setId(1L);
        product.setName("Notebook");
        product.setSalePrice(1200.0);
        List<Product> products = List.of(product);

        when(repository.findAllWithStock()).thenReturn(products);

        List<Product> result = service.getAllWithStock();

        assertEquals(products, result);
        verify(repository).findAllWithStock();
        verifyNoMoreInteractions(repository);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run:

```bash
mvn -Dtest=ProductServiceImpTest test
```

Expected: compilation fails because `ProductRepository.findAllWithStock()` and `ProductServiceImp.getAllWithStock()` do not exist.

---

### Task 2: Implement Repository And Service Flow

**Files:**
- Modify: `src/main/java/com/sistema_contable/sistema/contable/repository/ProductRepository.java`
- Modify: `src/main/java/com/sistema_contable/sistema/contable/services/interfaces/ProductService.java`
- Modify: `src/main/java/com/sistema_contable/sistema/contable/services/ProductServiceImp.java`

- [ ] **Step 1: Add repository query**

In `src/main/java/com/sistema_contable/sistema/contable/repository/ProductRepository.java`, add `java.util.List` and the query:

```java
package com.sistema_contable.sistema.contable.repository;

import java.util.List;

import com.sistema_contable.sistema.contable.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Product searchById(@Param("id") Long id);

    @Query("SELECT p FROM Product p WHERE p.name = :name")
    Product searchByName(@Param("name") String name);

    @Query("SELECT p FROM Product p WHERE (SELECT COALESCE(SUM(l.stock), 0) FROM Lot l WHERE l.product = p) > 0")
    List<Product> findAllWithStock();
}
```

- [ ] **Step 2: Add service interface method**

In `src/main/java/com/sistema_contable/sistema/contable/services/interfaces/ProductService.java`, add the method after `getAll()`:

```java
List<Product> getAllWithStock() throws Exception;
```

The interface should contain:

```java
public interface ProductService {
    void create(Product product) throws Exception;
    Product create(Product product, List<Payment> payments, User user) throws Exception;
    Product addLot(Long productId, Lot lot, List<Payment> payments, User user) throws Exception;
    List<Product> getAll() throws Exception;
    List<Product> getAllWithStock() throws Exception;
    Product searchById(Long id) throws Exception;
    Product searchByName(String name) throws Exception;
    Integer totalStock(Product product);
    void delete(Long id) throws Exception;
}
```

- [ ] **Step 3: Implement service method**

In `src/main/java/com/sistema_contable/sistema/contable/services/ProductServiceImp.java`, add this method in the `//GETTERS` section after `getAll()`:

```java
@Override
public List<Product> getAllWithStock() throws Exception {
    return repository.findAllWithStock();
}
```

- [ ] **Step 4: Run the service test**

Run:

```bash
mvn -Dtest=ProductServiceImpTest test
```

Expected: PASS.

- [ ] **Step 5: Commit service flow**

Stage only the files in this task and the service test:

```bash
git add src/main/java/com/sistema_contable/sistema/contable/repository/ProductRepository.java src/main/java/com/sistema_contable/sistema/contable/services/interfaces/ProductService.java src/main/java/com/sistema_contable/sistema/contable/services/ProductServiceImp.java src/test/java/com/sistema_contable/sistema/contable/services/ProductServiceImpTest.java
git commit -m "feat: add products with stock service flow"
```

---

### Task 3: Add Failing Resource Test

**Files:**
- Create: `src/test/java/com/sistema_contable/sistema/contable/resources/ProductResourceTest.java`

- [ ] **Step 1: Write the failing resource test**

Create `src/test/java/com/sistema_contable/sistema/contable/resources/ProductResourceTest.java`:

```java
package com.sistema_contable.sistema.contable.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import com.sistema_contable.sistema.contable.dto.ProductResponseDTO;
import com.sistema_contable.sistema.contable.model.Lot;
import com.sistema_contable.sistema.contable.model.Product;
import com.sistema_contable.sistema.contable.model.User;
import com.sistema_contable.sistema.contable.services.interfaces.ProductService;
import com.sistema_contable.sistema.contable.services.security.interfaces.AuthorizationService;

import static org.mockito.Mockito.mock;

class ProductResourceTest {

    @Test
    void getAllWithStockAuthorizesSellerAndReturnsProductResponses() throws Exception {
        ProductService service = mock(ProductService.class);
        AuthorizationService authService = mock(AuthorizationService.class);
        ProductResource resource = new ProductResource();
        ReflectionTestUtils.setField(resource, "service", service);
        ReflectionTestUtils.setField(resource, "authService", authService);

        Product product = new Product();
        product.setId(1L);
        product.setName("Notebook");
        product.setSalePrice(1200.0);
        Lot lot = new Lot();
        lot.setId(10L);
        lot.setUnitPrice(700.0);
        lot.setStock(3);
        product.addLot(lot);

        when(authService.sellerAuthorize("Bearer token")).thenReturn(new User());
        when(service.getAllWithStock()).thenReturn(List.of(product));
        when(service.totalStock(product)).thenReturn(3);

        ResponseEntity<?> response = resource.getAllWithStock("Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        @SuppressWarnings("unchecked")
        List<ProductResponseDTO> body = (List<ProductResponseDTO>) response.getBody();
        assertEquals(1, body.size());
        assertEquals(1L, body.get(0).getId());
        assertEquals("Notebook", body.get(0).getName());
        assertEquals(1200.0, body.get(0).getSalePrice());
        assertEquals(3, body.get(0).getTotalStock());
        assertEquals(1, body.get(0).getLots().size());
        assertEquals(10L, body.get(0).getLots().get(0).getId());
        assertEquals(3, body.get(0).getLots().get(0).getStock());
        assertEquals(1L, body.get(0).getLots().get(0).getProductId());
        verify(authService).sellerAuthorize("Bearer token");
        verify(service).getAllWithStock();
        verify(service).totalStock(product);
    }
}
```

- [ ] **Step 2: Run the resource test to verify it fails**

Run:

```bash
mvn -Dtest=ProductResourceTest test
```

Expected: compilation fails because `ProductResource.getAllWithStock(String token)` does not exist.

---

### Task 4: Implement Resource Endpoint

**Files:**
- Modify: `src/main/java/com/sistema_contable/sistema/contable/resources/ProductResource.java`

- [ ] **Step 1: Add the endpoint**

In `src/main/java/com/sistema_contable/sistema/contable/resources/ProductResource.java`, add this method after the existing `getAll` endpoint:

```java
@GetMapping(path = "/with-stock", produces = "application/json")
public ResponseEntity<?> getAllWithStock(@RequestHeader("Authorization") String token) {
    try {
        authService.sellerAuthorize(token);
        return new ResponseEntity<>(productResponse(service.getAllWithStock()), HttpStatus.OK);
    } catch (ModelExceptions modelError) {
        System.out.println(modelError.getMessage());
        return new ResponseEntity<>(null, modelError.getHttpStatus());
    } catch (Exception e) {
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```

- [ ] **Step 2: Run the resource test**

Run:

```bash
mvn -Dtest=ProductResourceTest test
```

Expected: PASS.

- [ ] **Step 3: Run both product tests**

Run:

```bash
mvn -Dtest=ProductServiceImpTest,ProductResourceTest test
```

Expected: PASS.

- [ ] **Step 4: Commit resource endpoint**

Stage only the resource and resource test:

```bash
git add src/main/java/com/sistema_contable/sistema/contable/resources/ProductResource.java src/test/java/com/sistema_contable/sistema/contable/resources/ProductResourceTest.java
git commit -m "feat: add products with stock endpoint"
```

---

### Task 5: Final Verification

**Files:**
- No new files.

- [ ] **Step 1: Run the full test suite**

Run:

```bash
mvn test
```

Expected: BUILD SUCCESS.

- [ ] **Step 2: Check final git status**

Run:

```bash
git status --short
```

Expected: only pre-existing unrelated dirty files remain, or a clean status if those were resolved outside this plan.

- [ ] **Step 3: If final verification passes, report endpoint contract**

Report:

```text
Added GET /products/with-stock.
Authorization: seller token through Authorization header.
Response: same ProductResponseDTO list as GET /products, filtered to total stock > 0.
Verification: mvn test passed.
```
