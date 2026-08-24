package com.urbankashi.pos.repository;

import com.urbankashi.pos.model.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {
    List<InvoiceItem> findByInvoiceId(Long invoiceId);

    @Modifying
    @Query("UPDATE InvoiceItem ii SET ii.variant = null WHERE ii.variant.id = :variantId")
    void detachVariant(@Param("variantId") Long variantId);

    @Modifying
    @Query("UPDATE InvoiceItem ii SET ii.variant = null WHERE ii.variant.product.id = :productId")
    void detachProductVariants(@Param("productId") Long productId);
}
