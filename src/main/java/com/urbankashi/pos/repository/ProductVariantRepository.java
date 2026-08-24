package com.urbankashi.pos.repository;

import com.urbankashi.pos.model.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    Optional<ProductVariant> findByBarcode(String barcode);

    List<ProductVariant> findByProductId(Long productId);

    @Query("SELECT pv FROM ProductVariant pv WHERE pv.stockQuantity <= :threshold")
    List<ProductVariant> findLowStock(@Param("threshold") int threshold);

    List<ProductVariant> findByProductIdAndColor(Long productId, String color);
}
