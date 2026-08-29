package com.urbankashi.pos.repository;

import com.urbankashi.pos.model.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.time.LocalDateTime;
import com.urbankashi.pos.model.StockMovementType;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findTop100ByOrderByCreatedAtDesc();
    List<StockMovement> findTop50ByVariantIdOrderByCreatedAtDesc(Long variantId);
    List<StockMovement> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime from, LocalDateTime to);
    List<StockMovement> findByTypeAndCreatedAtBetweenOrderByCreatedAtDesc(StockMovementType type, LocalDateTime from, LocalDateTime to);
}
