// USB/Bluetooth barcode scanners work as keyboard emulators
// They type characters very rapidly (< 50ms between keystrokes) and end with Enter

const BarcodeScanner = {
    buffer: '',
    lastKeyTime: 0,
    cameraStream: null,
    cameraFrameId: null,
    zxingControls: null,
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
            const barcodeInput = document.getElementById('barcodeInput');
            const barcode = activeEl?.id === 'barcodeInput'
                ? barcodeInput.value.trim()
                : this.buffer;

            if (barcode.length >= this.MIN_LENGTH) {
                this.processScan(barcode);
            }
            if (barcodeInput) barcodeInput.value = '';
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
        const normalizedBarcode = this.extractBarcode(barcode);
        console.log('📦 Scanned barcode:', normalizedBarcode);
        try {
            const response = await fetch(`/api/scan?barcode=${encodeURIComponent(normalizedBarcode)}`);
            if (response.ok) {
                const product = await response.json();
                CartManager.addItem(product);
                showToast(`Added: ${product.productName} (${product.size}/${product.color})`, 'success');
            } else {
                showToast(`Product not found for barcode: ${normalizedBarcode}`, 'error');
            }
        } catch (error) {
            console.error('Scan error:', error);
            showToast('Error scanning product', 'error');
        }
    },

    extractBarcode(scanValue) {
        const value = scanValue.trim();
        const payload = value.split('|');
        return payload[0] === 'UKPOS' && payload[1] ? payload[1].trim() : value;
    },

    async openCameraScanner() {
        if (!navigator.mediaDevices?.getUserMedia) {
            showToast('Camera access requires a secure HTTPS connection or localhost. Please use chrome://flags to allow this IP.', 'error');
        }
        if (!('BarcodeDetector' in window) && !window.ZXingBrowser) {
            showToast('Camera scanner is still loading. Check the internet connection and try again.', 'error');
            return;
        }

        const modalElement = document.getElementById('cameraScannerModal');
        const video = document.getElementById('cameraScannerPreview');
        const status = document.getElementById('cameraScannerStatus');
        const modal = bootstrap.Modal.getOrCreateInstance(modalElement);
        modalElement.addEventListener('hidden.bs.modal', () => this.stopCameraScanner(), { once: true });
        modal.show();

        try {
            status.textContent = 'Allow camera access, then point it at the code.';
            if (window.ZXingBrowser) {
                const reader = new ZXingBrowser.BrowserMultiFormatReader();
                this.zxingControls = await reader.decodeFromConstraints(
                    { video: { facingMode: { ideal: 'environment' } }, audio: false },
                    video,
                    (result, error) => {
                        if (!result) return;
                        this.stopCameraScanner();
                        modal.hide();
                        this.processScan(result.getText());
                    }
                );
                status.textContent = 'Scanning…';
                return;
            }

            this.cameraStream = await navigator.mediaDevices.getUserMedia({
                video: { facingMode: { ideal: 'environment' } },
                audio: false
            });
            video.srcObject = this.cameraStream;
            await video.play();

            const formats = await BarcodeDetector.getSupportedFormats();
            const supportedFormats = ['qr_code', 'code_128', 'code_39', 'ean_13', 'ean_8', 'upc_a', 'upc_e']
                .filter(format => formats.includes(format));
            const detector = new BarcodeDetector(supportedFormats.length ? { formats: supportedFormats } : undefined);
            status.textContent = 'Scanning…';

            const scanFrame = async () => {
                if (!this.cameraStream) return;
                try {
                    const codes = await detector.detect(video);
                    if (codes.length > 0 && codes[0].rawValue) {
                        const scanValue = codes[0].rawValue;
                        this.stopCameraScanner();
                        modal.hide();
                        this.processScan(scanValue);
                        return;
                    }
                } catch (error) {
                    console.error('Camera scan error:', error);
                }
                this.cameraFrameId = requestAnimationFrame(scanFrame);
            };
            scanFrame();
        } catch (error) {
            console.error('Camera access error:', error);
            this.stopCameraScanner();
            modal.hide();
            showToast('Camera access was blocked. Use Chrome, Edge, or Safari outside VS Code and allow webcam permission.', 'error');
        }
    },

    stopCameraScanner() {
        if (this.cameraFrameId) cancelAnimationFrame(this.cameraFrameId);
        this.cameraFrameId = null;
        if (this.zxingControls) this.zxingControls.stop();
        this.zxingControls = null;
        if (this.cameraStream) this.cameraStream.getTracks().forEach(track => track.stop());
        this.cameraStream = null;
        const video = document.getElementById('cameraScannerPreview');
        if (video) video.srcObject = null;
    }
};

document.addEventListener('DOMContentLoaded', () => BarcodeScanner.init());

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker.register('/sw.js');
  });
}
