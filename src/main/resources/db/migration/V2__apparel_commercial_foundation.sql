ALTER TABLE product_variants ADD COLUMN sku VARCHAR(100);
ALTER TABLE invoice_items ADD COLUMN cost_price NUMERIC(10,2);
ALTER TABLE invoices ADD COLUMN cashier_username VARCHAR(100);

UPDATE product_variants
SET sku = 'UK-' || id
WHERE sku IS NULL;

CREATE UNIQUE INDEX uk_product_variants_sku ON product_variants(sku);
CREATE INDEX idx_invoices_cashier_username ON invoices(cashier_username);
