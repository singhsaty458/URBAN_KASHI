async function sendWhatsAppBill(customerPhone, invoiceNumber, grandTotal, items, invoiceId, discountAmount) {
    if (!customerPhone) return showToast('No customer phone number available', 'error');
    let phone = customerPhone.replace(/\D/g, '');
    if (phone.length === 10) phone = '91' + phone;

    let message = `🛒 *URBAN KASHI*\n━━━━━━━━━━━━━━━\n📄 Invoice: *${invoiceNumber}*\n📅 Date: ${new Date().toLocaleDateString('en-IN')}\n━━━━━━━━━━━━━━━\n\n`;
    if (items?.length) {
        message += '*Items:*\n';
        items.forEach((item, index) => message += `${index + 1}. ${item.productName} (${item.size}/${item.color})\n   Qty: ${item.quantity} × ₹${Number(item.unitPrice).toFixed(2)} = ₹${Number(item.total).toFixed(2)}\n`);
        message += '\n';
    }
    message += `━━━━━━━━━━━━━━━\n`;
    if (Number(discountAmount) > 0) message += `🎁 Discount: -₹${Number(discountAmount).toFixed(2)}\n`;
    message += `💰 *Grand Total: ₹${Number(grandTotal).toFixed(2)}*\n━━━━━━━━━━━━━━━\n\n📎 Your PDF receipt is attached.\n\nThank you for shopping at Urban Kashi! 🙏`;

    showToast('Preparing PDF receipt...', 'info');
    try {
        const cloudResponse = await fetch(`/api/invoice/${invoiceId}/whatsapp?phone=${encodeURIComponent(phone)}`, {
            method: 'POST', headers: { [CartManager.csrfHeader()]: CartManager.csrfToken() }
        });
        if (cloudResponse.ok) return showToast('PDF receipt sent on WhatsApp!', 'success');

        const pdfResponse = await fetch(`/invoice/${invoiceId}/pdf`);
        if (!pdfResponse.ok) throw new Error('Unable to generate PDF receipt');
        const blob = await pdfResponse.blob();
        const file = new File([blob], `${invoiceNumber}.pdf`, { type: 'application/pdf' });
        if (navigator.share && (!navigator.canShare || navigator.canShare({ files: [file] }))) {
            await navigator.share({ title: `Urban Kashi ${invoiceNumber}`, text: message, files: [file] });
            return showToast('PDF shared successfully.', 'success');
        }

        const downloadUrl = URL.createObjectURL(blob);
        const anchor = document.createElement('a');
        anchor.href = downloadUrl; anchor.download = `${invoiceNumber}.pdf`; anchor.click();
        setTimeout(() => URL.revokeObjectURL(downloadUrl), 3000);
        window.open(`https://wa.me/${phone}?text=${encodeURIComponent(message + '\nPlease attach the downloaded PDF receipt.')}`, '_blank');
        showToast('PDF downloaded. Attach it in the opened WhatsApp chat.', 'info');
    } catch (error) {
        if (error.name !== 'AbortError') {
            console.error('WhatsApp PDF share failed:', error);
            showToast(error.message || 'Unable to share PDF receipt', 'error');
        }
    }
}

function sendLastInvoiceWhatsApp() {
    const invoice = CartManager._lastInvoice;
    if (!invoice) return showToast('No recent invoice found', 'error');
    sendWhatsAppBill(invoice.customerPhone, invoice.invoiceNumber, invoice.grandTotal, invoice.items, invoice.invoiceId, invoice.discount);
}
