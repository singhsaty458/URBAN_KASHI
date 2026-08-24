// USB/Bluetooth barcode scanners work as keyboard emulators
// They type characters very rapidly (< 50ms between keystrokes) and end with Enter

const BarcodeScanner = {
    buffer: '',
    lastKeyTime: 0,
    THRESHOLD: 50, // ms - max time between keystrokes for scanner detection
    MIN_LENGTH: 3, // minimum barcode length
    
    init() {
        document.addEventListener('keydown', this.handleKeyDown.bind(this));
        console.log('🔫 Barcode Scanner Listener Active');
    },
    
    handleKeyDown(event) {
        const currentTime = Date.now();
        const timeDelta = currentTime - this.lastKeyTime;
        
        // If the active element is an input/textarea and it's not the barcode field, ignore
        const activeEl = document.activeElement;
        const isTypingInField = activeEl && (activeEl.tagName === 'INPUT' || activeEl.tagName === 'TEXTAREA') && activeEl.id !== 'barcodeInput';
        if (isTypingInField) return;
        
        if (event.key === 'Enter') {
            event.preventDefault();
            if (this.buffer.length >= this.MIN_LENGTH) {
                this.processScan(this.buffer);
            }
            this.buffer = '';
            return;
        }
        
        // If too much time passed, reset buffer (manual typing)
        if (timeDelta > this.THRESHOLD && this.buffer.length > 0) {
            this.buffer = '';
        }
        
        // Only capture printable characters
        if (event.key.length === 1) {
            this.buffer += event.key;
            this.lastKeyTime = currentTime;
        }
    },
    
    async processScan(barcode) {
        console.log('📦 Scanned barcode:', barcode);
        try {
            const response = await fetch(`/api/scan?barcode=${encodeURIComponent(barcode)}`);
            if (response.ok) {
                const product = await response.json();
                CartManager.addItem(product);
                showToast(`Added: ${product.productName} (${product.size}/${product.color})`, 'success');
            } else {
                showToast(`Product not found for barcode: ${barcode}`, 'error');
            }
        } catch (error) {
            console.error('Scan error:', error);
            showToast('Error scanning product', 'error');
        }
    }
};

document.addEventListener('DOMContentLoaded', () => BarcodeScanner.init());
