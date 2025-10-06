package com.example.currencyapp.model;


public class DepositRequest {
    private String username;
    private double amount;
    private String deposit;
    
    public DepositRequest() {}
    
    public DepositRequest(String username, double amount, String deposit) {
        this.username = username;
        this.amount = amount;
        this.deposit = deposit;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getDeposit() {
        return deposit;
    }
    
    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }
}