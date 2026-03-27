package com.express.expressbackend.domain.payment;

public class CreateOrderResponse {

    private String orderId;
    private long amount;
    private String currency;
    private String keyId;

    public CreateOrderResponse(String orderId, long amount, String currency, String keyId) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.keyId = keyId;
    }

    public String getOrderId() { return orderId; }
    public long getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getKeyId() { return keyId; }
}