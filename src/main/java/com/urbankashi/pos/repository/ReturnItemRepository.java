package com.urbankashi.pos.repository;

import com.urbankashi.pos.model.ReturnItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReturnItemRepository extends JpaRepository<ReturnItem, Long> {
    @Query("SELECT COALESCE(SUM(ri.quantity), 0) FROM ReturnItem ri WHERE ri.invoiceItem.id = :invoiceItemId")
    Integer sumReturnedQuantity(@Param("invoiceItemId") Long invoiceItemId);
}
