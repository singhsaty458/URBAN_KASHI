package com.urbankashi.pos.service;

import com.urbankashi.pos.repository.ProductVariantRepository;
import com.urbankashi.pos.repository.StockMovementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @Mock ProductVariantRepository repository;
    @Mock StockMovementRepository stockMovementRepository;
    @Mock AuditService auditService;

    @Test
    void rejectsNonPositiveStockDeduction() {
        InventoryService service = new InventoryService(repository, stockMovementRepository, auditService);
        assertThrows(IllegalArgumentException.class, () -> service.deductStock(1L, 0));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsNonPositiveStockAddition() {
        InventoryService service = new InventoryService(repository, stockMovementRepository, auditService);
        assertThrows(IllegalArgumentException.class, () -> service.addStock(1L, 0));
        verifyNoInteractions(repository);
    }
}
