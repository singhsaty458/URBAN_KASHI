const fs = require('fs');
const file = 'src/main/resources/static/js/whatsapp-share.js';
let content = fs.readFileSync(file, 'utf8');

content = content.replace(
    'function sendWhatsAppBill(customerPhone, invoiceNumber, grandTotal, items, invoiceId) {',
    'function sendWhatsAppBill(customerPhone, invoiceNumber, grandTotal, items, invoiceId, discountAmount) {'
);

content = content.replace(
    'sendWhatsAppBill(invoice.customerPhone, invoice.invoiceNumber, invoice.grandTotal, invoice.items, invoice.invoiceId);',
    'sendWhatsAppBill(invoice.customerPhone, invoice.invoiceNumber, invoice.grandTotal, invoice.items, invoice.invoiceId, invoice.discount);'
);

const totalBlock =     message += \â” â” â” â” â” â” â” â” â” â” â” â” â” â” â” \\n\;
    message += \ðŸ’° *Grand Total: â‚¹\*\\n\;;

const newTotalBlock =     message += \â” â” â” â” â” â” â” â” â” â” â” â” â” â” â” \\n\;
    if (discountAmount && parseFloat(discountAmount) > 0) {
        message += \ðŸŽ  *Discount Applied:* -â‚¹\\\n\;
        message += \ðŸ¥³ *You Saved:* â‚¹\!\\n\;
        message += \â” â” â” â” â” â” â” â” â” â” â” â” â” â” â” \\n\;
    }
    message += \ðŸ’° *Grand Total: â‚¹\*\\n\;;

content = content.replace(totalBlock, newTotalBlock);
fs.writeFileSync(file, content, 'utf8');
