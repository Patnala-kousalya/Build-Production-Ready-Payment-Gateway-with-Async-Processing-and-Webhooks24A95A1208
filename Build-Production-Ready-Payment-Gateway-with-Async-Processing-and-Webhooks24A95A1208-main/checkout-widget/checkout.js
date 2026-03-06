// Simple Payment Gateway SDK
(function(window) {
    'use strict';

    function PaymentGateway(options) {
        this.key = options.key;
        this.orderId = options.orderId;
        this.onSuccess = options.onSuccess || function() {};
        this.onFailure = options.onFailure || function() {};
        this.onClose = options.onClose || function() {};
        this.modal = null;
        this.iframe = null;
    }

    PaymentGateway.prototype.open = function() {
        // Create modal
        this.modal = document.createElement('div');
        this.modal.setAttribute('data-testid', 'payment-modal');
        this.modal.style.cssText = `
            position: fixed; top: 0; left: 0; width: 100%; height: 100%;
            background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center;
            z-index: 1000;
        `;

        // Create iframe
        this.iframe = document.createElement('iframe');
        this.iframe.setAttribute('data-testid', 'payment-iframe');
        this.iframe.src = 'http://localhost:3001?embedded=true&orderId=' + this.orderId;
        this.iframe.style.cssText = 'width: 400px; height: 600px; border: none;';

        // Create close button
        const closeButton = document.createElement('button');
        closeButton.setAttribute('data-testid', 'close-modal-button');
        closeButton.textContent = '×';
        closeButton.style.cssText = `
            position: absolute; top: 10px; right: 10px; background: red; color: white;
            border: none; width: 30px; height: 30px; cursor: pointer;
        `;
        closeButton.onclick = () => this.close();

        // Assemble modal
        this.modal.appendChild(this.iframe);
        this.modal.appendChild(closeButton);
        document.body.appendChild(this.modal);

        // Listen for messages
        window.addEventListener('message', this.handleMessage.bind(this));
    };

    PaymentGateway.prototype.close = function() {
        if (this.modal) {
            document.body.removeChild(this.modal);
            this.modal = null;
            this.onClose();
        }
    };

    PaymentGateway.prototype.handleMessage = function(event) {
        if (event.origin !== 'http://localhost:3001') return;

        switch (event.data.type) {
            case 'payment_success':
                this.onSuccess(event.data);
                this.close();
                break;
            case 'payment_failed':
                this.onFailure(event.data);
                this.close();
                break;
            case 'close_modal':
                this.close();
                break;
        }
    };

    // Expose globally
    window.PaymentGateway = PaymentGateway;

})(window);