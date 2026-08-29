-- Urban Kashi POS PostgreSQL baseline schema
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    hsn_code VARCHAR(20),
    gst_rate NUMERIC(5,2),
    brand VARCHAR(100),
    category VARCHAR(100),
    image_url VARCHAR(500),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE product_images (
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    image_url VARCHAR(255)
);

CREATE TABLE product_variants (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id),
    barcode VARCHAR(100) UNIQUE,
    size VARCHAR(10),
    color VARCHAR(50),
    selling_price NUMERIC(10,2),
    cost_price NUMERIC(10,2),
    stock_quantity INTEGER DEFAULT 0,
    image_url VARCHAR(500)
);

CREATE TABLE variant_images (
    variant_id BIGINT NOT NULL REFERENCES product_variants(id) ON DELETE CASCADE,
    image_url VARCHAR(255)
);

CREATE TABLE customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255),
    phone_number VARCHAR(15) UNIQUE,
    email VARCHAR(255),
    loyalty_points INTEGER DEFAULT 0,
    credit_balance NUMERIC(10,2) DEFAULT 0,
    address VARCHAR(500),
    created_at TIMESTAMP
);

CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT REFERENCES customers(id) ON DELETE SET NULL,
    total_taxable NUMERIC(10,2),
    total_cgst NUMERIC(10,2),
    total_sgst NUMERIC(10,2),
    grand_total NUMERIC(10,2),
    discount NUMERIC(10,2) DEFAULT 0,
    payment_mode VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP
);

CREATE TABLE invoice_items (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
    variant_id BIGINT REFERENCES product_variants(id) ON DELETE SET NULL,
    product_name VARCHAR(255),
    size VARCHAR(255),
    color VARCHAR(255),
    quantity INTEGER,
    unit_price NUMERIC(10,2),
    taxable_amount NUMERIC(10,2),
    cgst_amount NUMERIC(10,2),
    sgst_amount NUMERIC(10,2),
    total_amount NUMERIC(10,2)
);

CREATE TABLE return_transactions (
    id BIGSERIAL PRIMARY KEY,
    return_number VARCHAR(50) NOT NULL UNIQUE,
    invoice_id BIGINT NOT NULL REFERENCES invoices(id),
    refund_amount NUMERIC(10,2) NOT NULL,
    refund_mode VARCHAR(20) NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE return_items (
    id BIGSERIAL PRIMARY KEY,
    return_transaction_id BIGINT NOT NULL REFERENCES return_transactions(id) ON DELETE CASCADE,
    invoice_item_id BIGINT NOT NULL REFERENCES invoice_items(id),
    quantity INTEGER NOT NULL,
    refund_amount NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_invoices_created_at ON invoices(created_at);
CREATE INDEX idx_invoice_items_invoice_id ON invoice_items(invoice_id);
CREATE INDEX idx_return_items_invoice_item_id ON return_items(invoice_item_id);
CREATE INDEX idx_product_variants_product_id ON product_variants(product_id);

CREATE TABLE stock_movements (
    id BIGSERIAL PRIMARY KEY,
    variant_id BIGINT NOT NULL REFERENCES product_variants(id),
    type VARCHAR(20) NOT NULL,
    quantity_change INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reference VARCHAR(100),
    reason VARCHAR(500),
    performed_by VARCHAR(100),
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_stock_movements_variant_id ON stock_movements(variant_id);
CREATE INDEX idx_stock_movements_created_at ON stock_movements(created_at);

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id VARCHAR(80),
    performed_by VARCHAR(100),
    details VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
