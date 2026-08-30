package com.urbankashi.pos.repository;

import com.urbankashi.pos.model.ExchangeTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeTransactionRepository extends JpaRepository<ExchangeTransaction, Long> {}
