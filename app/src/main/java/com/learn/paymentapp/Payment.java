package com.learn.paymentapp;

public class Payment {

    private String type;
    private int amount;
    private String provider;
    private String transactionRef;

    public Payment(String type, int amount, String provider, String transactionRef) {
        this.type = type;
        this.amount = amount;
        this.provider = provider;
        this.transactionRef = transactionRef;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public String getProvider() {
        return provider;
    }

    public String getTransactionRef() {
        return transactionRef;
    }
}