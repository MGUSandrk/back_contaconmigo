package com.sistema_contable.sistema.contable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sistema_contable.sistema.contable.model.sales.Invoice;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByClientCuit(String clientCuit);

    @Query("SELECT DISTINCT i FROM Invoice i LEFT JOIN FETCH i.items WHERE i.id = :id")
    Invoice searchById(@Param("id") Long id);

}
