package com.example.currencyapp.model;


import jakarta.persistence.*;

@Entity
@Table(name = "user_accounts")
@IdClass(UserAccountId.class)
public class UserAccount {
    
    @Id
    private String username;
    
    @Id
    @Column(name = "currency_code")
    private String currencyCode;
    
    private double balance;
    
    public UserAccount() {}
    
    public UserAccount(String username, String currencyCode, double balance) {
        this.username = username;
        this.currencyCode = currencyCode;
        this.balance = balance;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    public double getBalance() {
        return balance;
    }
    
    public void setBalance(double balance) {
        this.balance = balance;
    }
}
