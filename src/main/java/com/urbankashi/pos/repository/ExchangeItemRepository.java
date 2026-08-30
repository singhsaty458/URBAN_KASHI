package com.urbankashi.pos.repository;

import com.urbankashi.pos.model.ExchangeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {
    @Query("select coalesce(sum(e.quantity), 0) from ExchangeItem e where e.originalInvoiceItem.id = :invoiceItemId")
    Integer sumExchangedQuantity(@Param("invoiceItemId") Long invoiceItemId);
}
