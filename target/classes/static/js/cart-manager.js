function showToast(message, type = 'info') {
    const container = document.getElementById('toastContainer') || createToastContainer();
    const toast = document.createElement('div');
    toast.className = `toast-notification toast-${type}`;
    toast.innerHTML = `
        <span>${type === 'success' ? '✅' : type === 'error' ? '❌' : 'ℹ️'} ${message}</span>
        <button onclick="this.parentElement.remove()" style="background:none;border:none;color:white;font-size:18px;cursor:pointer">×</button>
    `;
    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

function createToastContainer() {
    const container = document.createElement('div');
    container.id = 'toastContainer';
    container.style.cssText = 'position:fixed;top:20px;right:20px;z-index:10000;display:flex;flex-direction:column;gap:10px;';
    document.body.appendChild(container);
    return container;
}

const CartManager = {
    items: [], // Array of {variantId, productName, brand, size, color, barcode, sellingPrice, gstRate, quantity, stockQuantity}
    customerPhone: '',
    customerName: '',
    discount: 0,
    
    addItem(scanResult) {
        // scanResult is ScanResultDTO from /api/scan
        const existing = this.items.find(i => i.variantId === scanResult.variantId);
        if (existing) {
            if (existing.quantity >= scanResult.stockQuantity) {
                showToast('Maximum stock reached!', 'error');
                return;
            }
            existing.quantity++;
        } else {
            this.items.push({
                variantId: scanResult.variantId,
                productName: scanResult.productName,
                brand: scanResult.brand,
                size: scanResult.size,
                color: scanResult.color,
                barcode: scanResult.barcode,
                sellingPrice: parseFloat(scanResult.sellingPrice),
                gstRate: parseFloat(scanResult.gstRate) || 0,
                quantity: 1,
                stockQuantity: scanResult.stockQuantity
            });
        }
        this.renderCart();
    },
    
    removeItem(variantId) {
        this.items = this.items.filter(i => i.variantId !== variantId);
        this.renderCart();
    },
    
    updateQuantity(variantId, newQty) {
        const item = this.items.find(i => i.variantId === variantId);
        if (item) {
            if (newQty <= 0) {
                this.removeItem(variantId);
                return;
            }
            if (newQty > item.stockQuantity) {
                showToast('Exceeds available stock!', 'error');
                return;
            }
            item.quantity = newQty;
        }
        this.renderCart();
    },
    
    clearCart() {
        this.items = [];
        this.customerPhone = '';
        this.customerName = '';
        this.discount = 0;
        this.renderCart();
        document.getElementById('customerPhone')?.value && (document.getElementById('customerPhone').value = '');
        document.getElementById('customerName')?.value && (document.getElementById('customerName').value = '');
        document.getElementById('customerInfo')?.classList.add('d-none');
    },
    
    calculateTotals() {
        let subtotal = 0, totalTaxable = 0, totalCgst = 0, totalSgst = 0;
        
        this.items.forEach(item => {
            const lineTotal = item.sellingPrice * item.quantity;
            const taxableBase = lineTotal / (1 + item.gstRate / 100);
            const gstAmount = lineTotal - taxableBase;
            const cgst = gstAmount / 2;
            const sgst = gstAmount / 2;
            
            subtotal += lineTotal;
            totalTaxable += taxableBase;
            totalCgst += cgst;
            totalSgst += sgst;
        });
        
        const grandTotal = subtotal - this.discount;
        
        return {
            subtotal: subtotal.toFixed(2),
            totalTaxable: totalTaxable.toFixed(2),
            totalCgst: totalCgst.toFixed(2),
            totalSgst: totalSgst.toFixed(2),
            grandTotal: grandTotal.toFixed(2),
            itemCount: this.items.reduce((sum, i) => sum + i.quantity, 0)
        };
    },
    
    renderCart() {
        // Render cart table body
        const tbody = document.getElementById('cartTableBody');
        if (!tbody) return;
        
        tbody.innerHTML = '';
        
        this.items.forEach((item, index) => {
            const lineTotal = (item.sellingPrice * item.quantity).toFixed(2);
            const taxableBase = (item.sellingPrice * item.quantity / (1 + item.gstRate / 100)).toFixed(2);
            const gst = (item.sellingPrice * item.quantity - parseFloat(taxableBase)).toFixed(2);
            
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>
                    <strong>${item.productName}</strong><br>
                    <small class="text-muted">${item.brand || ''}</small>
                </td>
                <td><span class="badge bg-info">${item.size}</span></td>
                <td><span class="badge" style="background:${this.getColorBadge(item.color)}">${item.color}</span></td>
                <td>
                    <div class="input-group input-group-sm" style="width:100px">
                        <button class="btn btn-outline-light btn-sm" onclick="CartManager.updateQuantity(${item.variantId}, ${item.quantity - 1})">−</button>
                        <input type="number" class="form-control text-center bg-dark text-white" value="${item.quantity}" min="1" max="${item.stockQuantity}" onchange="CartManager.updateQuantity(${item.variantId}, parseInt(this.value))">
                        <button class="btn btn-outline-light btn-sm" onclick="CartManager.updateQuantity(${item.variantId}, ${item.quantity + 1})">+</button>
                    </div>
                </td>
                <td>₹${item.sellingPrice.toFixed(2)}</td>
                <td>₹${gst}</td>
                <td><strong>₹${lineTotal}</strong></td>
                <td>
                    <button class="btn btn-sm btn-outline-danger" onclick="CartManager.removeItem(${item.variantId})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
        
        // Update totals
        const totals = this.calculateTotals();
        this.updateElement('subtotalDisplay', '₹' + totals.subtotal);
        this.updateElement('taxableDisplay', '₹' + totals.totalTaxable);
        this.updateElement('cgstDisplay', '₹' + totals.totalCgst);
        this.updateElement('sgstDisplay', '₹' + totals.totalSgst);
        this.updateElement('grandTotalDisplay', '₹' + totals.grandTotal);
        this.updateElement('itemCountDisplay', totals.itemCount + ' items');
        
        // Empty cart message
        const emptyMsg = document.getElementById('emptyCartMsg');
        if (emptyMsg) emptyMsg.style.display = this.items.length === 0 ? 'block' : 'none';
    },
    
    updateElement(id, value) {
        const el = document.getElementById(id);
        if (el) el.textContent = value;
    },
    
    getColorBadge(color) {
        const colors = {
            'Red': '#dc3545', 'Blue': '#0d6efd', 'Green': '#198754', 'Black': '#212529',
            'White': '#f8f9fa', 'Navy': '#001f3f', 'Maroon': '#85144b', 'Yellow': '#ffc107',
            'Pink': '#e83e8c', 'Grey': '#6c757d', 'Beige': '#d4a574', 'Brown': '#795548',
            'Crimson': '#dc143c', 'Charcoal': '#36454f', 'Olive': '#808000', 'Teal': '#008080'
        };
        return colors[color] || '#6c757d';
    },
    
    async checkout(paymentMode) {
        if (this.items.length === 0) {
            showToast('Cart is empty!', 'error');
            return;
        }
        
        const payload = {
            items: this.items.map(i => ({ variantId: i.variantId, quantity: i.quantity })),
            customerPhone: this.customerPhone,
            customerName: this.customerName,
            paymentMode: paymentMode,
            discount: this.discount
        };
        
        try {
            const response = await fetch('/api/checkout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            
            if (response.ok) {
                const invoice = await response.json();
                showToast(`Invoice ${invoice.invoiceNumber} generated! Total: ₹${invoice.grandTotal}`, 'success');
                this.showInvoiceModal(invoice);
                this.clearCart();
            } else {
                const error = await response.json();
                showToast(error.message || 'Checkout failed!', 'error');
            }
        } catch (error) {
            console.error('Checkout error:', error);
            showToast('Network error during checkout', 'error');
        }
    },
    
    showInvoiceModal(invoice) {
        // Populate invoice modal with data
        document.getElementById('modalInvoiceNumber').textContent = invoice.invoiceNumber;
        document.getElementById('modalGrandTotal').textContent = '₹' + parseFloat(invoice.grandTotal).toFixed(2);
        document.getElementById('modalCustomerName').textContent = invoice.customerName || 'Walk-in Customer';
        document.getElementById('modalPaymentMode').textContent = invoice.paymentMode;
        document.getElementById('modalInvoiceId').value = invoice.invoiceId;
        
        // Store for WhatsApp
        this._lastInvoice = invoice;
        
        // Show modal
        const modal = new bootstrap.Modal(document.getElementById('invoiceSuccessModal'));
        modal.show();
    },
    
    setDiscount(amount) {
        this.discount = parseFloat(amount) || 0;
        this.renderCart();
    },
    
    async lookupCustomer(phone) {
        if (!phone || phone.length < 10) return;
        try {
            const response = await fetch(`/api/customer?phone=${encodeURIComponent(phone)}`);
            if (response.ok) {
                const customer = await response.json();
                this.customerPhone = customer.phoneNumber;
                this.customerName = customer.fullName;
                document.getElementById('customerName').value = customer.fullName;
                document.getElementById('customerLoyalty').textContent = customer.loyaltyPoints;
                document.getElementById('customerCredit').textContent = '₹' + parseFloat(customer.creditBalance).toFixed(2);
                document.getElementById('customerInfo').classList.remove('d-none');
                showToast(`Customer found: ${customer.fullName}`, 'success');
            } else {
                document.getElementById('customerInfo').classList.add('d-none');
                this.customerPhone = phone;
                this.customerName = document.getElementById('customerName').value;
            }
        } catch (e) {
            console.error('Customer lookup error:', e);
        }
    }
};

