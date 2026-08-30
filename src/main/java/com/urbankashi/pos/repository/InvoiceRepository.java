package com.urbankashi.pos.repository;

import com.urbankashi.pos.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    List<Invoice> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    List<Invoice> findByCustomerId(Long customerId);

    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.createdAt >= :start")
    Long countInvoicesSince(@Param("start") LocalDateTime start);

    @Query("SELECT COALESCE(SUM(i.grandTotal), 0) FROM Invoice i WHERE i.createdAt >= :start")
    BigDecimal sumRevenueSince(@Param("start") LocalDateTime start);

    List<Invoice> findTop10ByOrderByCreatedAtDesc();
    List<Invoice> findAllByOrderByCreatedAtDesc();

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE Invoice i SET i.customer = null WHERE i.customer.id = :customerId")
    void detachCustomer(@Param("customerId") Long customerId);
}
