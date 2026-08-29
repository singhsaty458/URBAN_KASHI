package com.urbankashi.pos.repository;

import com.urbankashi.pos.model.ReturnTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;

import java.util.List;

public interface ReturnTransactionRepository extends JpaRepository<ReturnTransaction, Long> {
    List<ReturnTransaction> findByInvoiceIdOrderByCreatedAtDesc(Long invoiceId);

    @Query("SELECT COALESCE(SUM(rt.refundAmount), 0) FROM ReturnTransaction rt WHERE rt.invoice.id = :invoiceId")
    BigDecimal sumRefundAmountByInvoiceId(@Param("invoiceId") Long invoiceId);
}
