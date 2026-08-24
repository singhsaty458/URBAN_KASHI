-- =============================================
-- URBAN KASHI POS - Sample Seed Data
-- H2 Compatible (MERGE INTO ... KEY() VALUES)
-- =============================================

-- Insert sample products
MERGE INTO products (id, name, hsn_code, gst_rate, brand, category, image_url, active, created_at, updated_at) KEY(id) VALUES
(1, 'Men''s Classic T-Shirt', '6109', 5.00, 'Urban Kashi', 'T-Shirts', null, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Men''s Slim Fit Jeans', '6204', 12.00, 'Urban Kashi', 'Jeans', null, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Women''s Kurti', '6204', 5.00, 'Urban Kashi', 'Ethnic Wear', null, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Men''s Formal Shirt', '6205', 5.00, 'Urban Kashi', 'Shirts', null, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Women''s Designer Saree', '5407', 5.00, 'Urban Kashi', 'Ethnic Wear', null, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Unisex Hoodie', '6110', 12.00, 'Urban Kashi', 'Winter Wear', null, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample variants (rich matrix for size x color grid)
-- Product 1: Men's Classic T-Shirt (Rs.499, GST 5%)
MERGE INTO product_variants (id, product_id, barcode, size, color, selling_price, cost_price, stock_quantity) KEY(id) VALUES
(1,  1, 'UK001SBK', 'S',  'Black', 499.00, 250.00, 25),
(2,  1, 'UK001MBK', 'M',  'Black', 499.00, 250.00, 40),
(3,  1, 'UK001LBK', 'L',  'Black', 499.00, 250.00, 50),
(4,  1, 'UK001XLBK','XL', 'Black', 499.00, 250.00, 30),
(5,  1, 'UK001SWH', 'S',  'White', 499.00, 250.00, 20),
(6,  1, 'UK001MWH', 'M',  'White', 499.00, 250.00, 35),
(7,  1, 'UK001LWH', 'L',  'White', 499.00, 250.00, 45),
(8,  1, 'UK001XLWH','XL', 'White', 499.00, 250.00, 25),
(9,  1, 'UK001SNV', 'S',  'Navy',  499.00, 250.00, 15),
(10, 1, 'UK001MNV', 'M',  'Navy',  499.00, 250.00, 30),
(11, 1, 'UK001LNV', 'L',  'Navy',  499.00, 250.00, 35),
(12, 1, 'UK001XLNV','XL', 'Navy',  499.00, 250.00, 20),
(13, 1, 'UK001SRE', 'S',  'Red',   499.00, 250.00, 10),
(14, 1, 'UK001MRE', 'M',  'Red',   499.00, 250.00, 25),
(15, 1, 'UK001LRE', 'L',  'Red',   499.00, 250.00, 30),
(16, 1, 'UK001XLRE','XL', 'Red',   499.00, 250.00, 15);

-- Product 2: Men's Slim Fit Jeans (Rs.1299, GST 12%)
MERGE INTO product_variants (id, product_id, barcode, size, color, selling_price, cost_price, stock_quantity) KEY(id) VALUES
(17, 2, 'UK00228BL', '28', 'Blue',  1299.00, 650.00, 15),
(18, 2, 'UK00230BL', '30', 'Blue',  1299.00, 650.00, 25),
(19, 2, 'UK00232BL', '32', 'Blue',  1299.00, 650.00, 35),
(20, 2, 'UK00234BL', '34', 'Blue',  1299.00, 650.00, 30),
(21, 2, 'UK00236BL', '36', 'Blue',  1299.00, 650.00, 20),
(22, 2, 'UK00228BK', '28', 'Black', 1299.00, 650.00, 10),
(23, 2, 'UK00230BK', '30', 'Black', 1299.00, 650.00, 20),
(24, 2, 'UK00232BK', '32', 'Black', 1299.00, 650.00, 30),
(25, 2, 'UK00234BK', '34', 'Black', 1299.00, 650.00, 25),
(26, 2, 'UK00236BK', '36', 'Black', 1299.00, 650.00, 15),
(27, 2, 'UK00230GR', '30', 'Grey',  1299.00, 650.00, 18),
(28, 2, 'UK00232GR', '32', 'Grey',  1299.00, 650.00, 22),
(29, 2, 'UK00234GR', '34', 'Grey',  1299.00, 650.00, 20);

-- Product 3: Women's Kurti (Rs.799, GST 5%)
MERGE INTO product_variants (id, product_id, barcode, size, color, selling_price, cost_price, stock_quantity) KEY(id) VALUES
(30, 3, 'UK003SPI',  'S',  'Pink',   799.00, 400.00, 20),
(31, 3, 'UK003MPI',  'M',  'Pink',   799.00, 400.00, 30),
(32, 3, 'UK003LPI',  'L',  'Pink',   799.00, 400.00, 35),
(33, 3, 'UK003XLPI', 'XL', 'Pink',   799.00, 400.00, 25),
(34, 3, 'UK003SRE',  'S',  'Red',    799.00, 400.00, 15),
(35, 3, 'UK003MRE',  'M',  'Red',    799.00, 400.00, 25),
(36, 3, 'UK003LRE',  'L',  'Red',    799.00, 400.00, 30),
(37, 3, 'UK003XLRE', 'XL', 'Red',    799.00, 400.00, 20),
(38, 3, 'UK003SGN',  'S',  'Green',  799.00, 400.00, 20),
(39, 3, 'UK003MGN',  'M',  'Green',  799.00, 400.00, 28),
(40, 3, 'UK003LGN',  'L',  'Green',  799.00, 400.00, 32),
(41, 3, 'UK003SYL',  'S',  'Yellow', 799.00, 400.00, 18),
(42, 3, 'UK003MYL',  'M',  'Yellow', 799.00, 400.00, 22);

-- Product 4: Men's Formal Shirt (Rs.899, GST 5%)
MERGE INTO product_variants (id, product_id, barcode, size, color, selling_price, cost_price, stock_quantity) KEY(id) VALUES
(43, 4, 'UK004SWH',  'S',  'White', 899.00, 450.00, 15),
(44, 4, 'UK004MWH',  'M',  'White', 899.00, 450.00, 25),
(45, 4, 'UK004LWH',  'L',  'White', 899.00, 450.00, 30),
(46, 4, 'UK004XLWH', 'XL', 'White', 899.00, 450.00, 20),
(47, 4, 'UK004SBL',  'S',  'Blue',  899.00, 450.00, 12),
(48, 4, 'UK004MBL',  'M',  'Blue',  899.00, 450.00, 22),
(49, 4, 'UK004LBL',  'L',  'Blue',  899.00, 450.00, 28),
(50, 4, 'UK004XLBL', 'XL', 'Blue',  899.00, 450.00, 18),
(51, 4, 'UK004MPI',  'M',  'Pink',  899.00, 450.00, 15),
(52, 4, 'UK004LPI',  'L',  'Pink',  899.00, 450.00, 20);

-- Product 5: Women's Designer Saree (Rs.2499, GST 5%)
MERGE INTO product_variants (id, product_id, barcode, size, color, selling_price, cost_price, stock_quantity) KEY(id) VALUES
(53, 5, 'UK005FRE', 'FREE', 'Red',   2499.00, 1200.00, 12),
(54, 5, 'UK005FGN', 'FREE', 'Green', 2499.00, 1200.00, 10),
(55, 5, 'UK005FBL', 'FREE', 'Blue',  2499.00, 1200.00, 8),
(56, 5, 'UK005FGO', 'FREE', 'Gold',  2499.00, 1200.00, 5);

-- Product 6: Unisex Hoodie (Rs.1499, GST 12%)
MERGE INTO product_variants (id, product_id, barcode, size, color, selling_price, cost_price, stock_quantity) KEY(id) VALUES
(57, 6, 'UK006SBK',  'S',  'Black', 1499.00, 750.00, 15),
(58, 6, 'UK006MBK',  'M',  'Black', 1499.00, 750.00, 25),
(59, 6, 'UK006LBK',  'L',  'Black', 1499.00, 750.00, 30),
(60, 6, 'UK006XLBK', 'XL', 'Black', 1499.00, 750.00, 20),
(61, 6, 'UK006SGR',  'S',  'Grey',  1499.00, 750.00, 12),
(62, 6, 'UK006MGR',  'M',  'Grey',  1499.00, 750.00, 20),
(63, 6, 'UK006LGR',  'L',  'Grey',  1499.00, 750.00, 25),
(64, 6, 'UK006XLGR', 'XL', 'Grey',  1499.00, 750.00, 15),
(65, 6, 'UK006MNV',  'M',  'Navy',  1499.00, 750.00, 18),
(66, 6, 'UK006LNV',  'L',  'Navy',  1499.00, 750.00, 22),
(67, 6, 'UK006XLNV', 'XL', 'Navy',  1499.00, 750.00, 10);

-- Insert sample customers
MERGE INTO customers (id, full_name, phone_number, loyalty_points, credit_balance, created_at) KEY(id) VALUES
(1, 'Rahul Sharma',  '9876543210', 50,  0.00,   CURRENT_TIMESTAMP),
(2, 'Priya Singh',   '9876543211', 120, 500.00, CURRENT_TIMESTAMP),
(3, 'Amit Verma',    '9876543212', 30,  0.00,   CURRENT_TIMESTAMP),
(4, 'Sneha Gupta',   '9876543213', 75,  200.00, CURRENT_TIMESTAMP),
(5, 'Vikram Patel',  '9876543214', 200, 0.00,   CURRENT_TIMESTAMP);

-- Reset auto-increment sequences in H2 database to avoid key collision errors on insert
ALTER TABLE products ALTER COLUMN id RESTART WITH 100;
ALTER TABLE product_variants ALTER COLUMN id RESTART WITH 1000;
ALTER TABLE customers ALTER COLUMN id RESTART WITH 100;
