package com.express.expressbackend.domain.wallet;

public class WithdrawalRequest {

    private double amount;
    private String upiId;
    private String accountNumber;
    private String ifscCode;
    private String accountHolderName;
    private String paymentMethod; // "UPI" or "BANK"

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getIfscCode() { return ifscCode; }
    public void setIfscCode(String ifscCode) { this.ifscCode = ifscCode; }

    public String getAccountHolderName() { return accountHolderName; }
    public void setAccountHolderName(String accountHolderName) { this.accountHolderName = accountHolderName; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}