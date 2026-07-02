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
