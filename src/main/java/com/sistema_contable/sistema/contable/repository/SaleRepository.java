package com.sistema_contable.sistema.contable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sistema_contable.sistema.contable.model.sales.Sale;

import java.util.Date;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByClientId(Long clientId);

    @Query("SELECT s FROM Sale s WHERE s.dateCreated >= :startDate AND s.dateCreated < :endDate ORDER BY s.dateCreated DESC")
    List<Sale> findByDateCreatedBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.dateCreated >= :startDate AND s.dateCreated < :endDate")
    Long countSalesByDateCreatedBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

}
