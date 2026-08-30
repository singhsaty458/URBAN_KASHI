CREATE TABLE exchange_transactions (
    id BIGSERIAL PRIMARY KEY,
    exchange_number VARCHAR(40) NOT NULL UNIQUE,
    original_invoice_id BIGINT NOT NULL REFERENCES invoices(id),
    replacement_invoice_id BIGINT NOT NULL UNIQUE REFERENCES invoices(id),
    replacement_credit NUMERIC(12,2) NOT NULL,
    amount_paid NUMERIC(12,2) NOT NULL,
    reason VARCHAR(500),
    performed_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE exchange_items (
    id BIGSERIAL PRIMARY KEY,
    exchange_transaction_id BIGINT NOT NULL REFERENCES exchange_transactions(id),
    original_invoice_item_id BIGINT NOT NULL REFERENCES invoice_items(id),
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    credit_amount NUMERIC(12,2) NOT NULL
);

CREATE INDEX idx_exchange_original_invoice ON exchange_transactions(original_invoice_id);
CREATE INDEX idx_exchange_item_original ON exchange_items(original_invoice_item_id);
