# Products With Stock Endpoint Design

## Context

The backend is a Spring Boot layered application. Product HTTP behavior lives in `ProductResource`, business flow in `ProductService` and `ProductServiceImp`, and database access in `ProductRepository`.

Products do not store stock directly. Stock is stored in `Lot.stock`, and `ProductResponseDTO.totalStock` is currently calculated by `ProductService.totalStock(product)` from the product lots.

`GET /products` already returns `ProductResponseDTO` values and authorizes sellers with `authService.sellerAuthorize(token)`.

## Goal

Add an endpoint that returns the products whose total stock is greater than zero.

The endpoint will:

- Use the same response shape as `GET /products`.
- Include `lots` in each `ProductResponseDTO`.
- Calculate `totalStock` with the existing service method.
- Authorize with `sellerAuthorize`.
- Return an empty list with `200 OK` when no products have stock.

## Contract

Route:

```http
GET /products/with-stock
Authorization: Bearer <token>
```

Success response:

```http
200 OK
Content-Type: application/json
```

Body:

```json
[
  {
    "id": 1,
    "name": "Example",
    "salePrice": 100.0,
    "totalStock": 3,
    "lots": [
      {
        "id": 10,
        "unitPrice": 60.0,
        "stock": 3,
        "productId": 1
      }
    ]
  }
]
```

Errors follow the existing product resource pattern:

- Domain exceptions return their own `HttpStatus`.
- Unexpected exceptions return `500 INTERNAL_SERVER_ERROR`.

## Approach Options

### Option A: Repository Query With `JOIN`, `GROUP BY`, `HAVING`

Add `ProductRepository.findAllWithStock()` using JPQL:

```java
@Query("SELECT p FROM Product p JOIN p.lots l GROUP BY p HAVING SUM(l.stock) > 0")
List<Product> findAllWithStock();
```

The service exposes `getAllWithStock()` and delegates to the repository. The resource maps the returned products through the existing `productResponse` helpers.

This is the recommended approach because the database filters the records and the controller stays thin.

### Option B: Service Filters `repository.findAll()`

The service could load all products and keep only products where `totalStock(product) > 0`.

This is simpler to write but less efficient and spreads query intent into application logic.

### Option C: Native SQL Query

A native query could join `products` and `lots` by table and column names.

This gives exact SQL control, but JPQL is enough here because the query works on entity relationships and Java properties.

## Design

Use Option A.

Files to change:

- `src/main/java/com/sistema_contable/sistema/contable/repository/ProductRepository.java`
- `src/main/java/com/sistema_contable/sistema/contable/services/interfaces/ProductService.java`
- `src/main/java/com/sistema_contable/sistema/contable/services/ProductServiceImp.java`
- `src/main/java/com/sistema_contable/sistema/contable/resources/ProductResource.java`

Testing:

- Add a focused service test for `ProductServiceImp.getAllWithStock()` that verifies delegation to the repository and returned products.
- Add a focused resource test for `ProductResource.getAllWithStock()` that verifies seller authorization, service call, `200 OK`, and DTO shape.
- Run the project test suite with Maven.

## Notes

The repository query should not return products whose only lots have zero stock, null stock, or no lots. Existing product creation validates non-null stock, but the query should remain robust by relying on `SUM(l.stock) > 0`.

The new endpoint must be declared before `@DeleteMapping("/{id}")` is not required for Spring matching, but placing it near `getAll` keeps the resource readable.
