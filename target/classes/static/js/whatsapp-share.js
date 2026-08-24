function sendWhatsAppBill(customerPhone, invoiceNumber, grandTotal, items, invoiceId) {
    if (!customerPhone) {
        showToast('No customer phone number available', 'error');
        return;
    }
    
    // Clean phone number (remove +91, spaces, dashes)
    let phone = customerPhone.replace(/[\s\-\+]/g, '');
    if (phone.length === 10) phone = '91' + phone; // Add India country code
    
    const pdfUrl = window.location.origin + '/invoice/' + invoiceId + '/pdf';
    
    // Build invoice message
    let message = `🛍️ *URBAN KASHI*\n`;
    message += `━━━━━━━━━━━━━━━━\n`;
    message += `📄 Invoice: *${invoiceNumber}*\n`;
    message += `📅 Date: ${new Date().toLocaleDateString('en-IN')}\n`;
    message += `━━━━━━━━━━━━━━━━\n\n`;
    
    if (items && items.length > 0) {
        message += `*Items:*\n`;
        items.forEach((item, i) => {
            message += `${i+1}. ${item.productName} (${item.size}/${item.color})\n`;
            message += `   Qty: ${item.quantity} × ₹${parseFloat(item.unitPrice).toFixed(2)} = ₹${parseFloat(item.total).toFixed(2)}\n`;
        });
        message += `\n`;
    }
    
    message += `━━━━━━━━━━━━━━━━\n`;
    message += `💰 *Grand Total: ₹${parseFloat(grandTotal).toFixed(2)}*\n`;
    message += `━━━━━━━━━━━━━━━━\n\n`;
    message += `📄 *Download/View PDF Invoice:*\n`;
    message += `${pdfUrl}\n\n`;
    message += `Thank you for shopping at Urban Kashi! 🙏\n`;
    message += `Visit us again!`;
    
    // Encode and open WhatsApp
    const encodedMessage = encodeURIComponent(message);
    const whatsappUrl = `https://wa.me/${phone}?text=${encodedMessage}`;
    
    window.open(whatsappUrl, '_blank');
    showToast('Opening WhatsApp...', 'success');
}

// Helper to send from the invoice modal
function sendLastInvoiceWhatsApp() {
    const invoice = CartManager._lastInvoice;
    if (!invoice) {
        showToast('No recent invoice found', 'error');
        return;
    }
    sendWhatsAppBill(invoice.customerPhone, invoice.invoiceNumber, invoice.grandTotal, invoice.items, invoice.invoiceId);
}
