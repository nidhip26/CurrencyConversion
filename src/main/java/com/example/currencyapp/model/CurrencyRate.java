package com.example.currencyapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "currency_rates")
public class CurrencyRate {
    
    @Id
    @Column(name = "currency_code")
    private String currencyCode;
    
    @Column(name = "rate")
    private Double rate;
    
    @Column(name = "date")
    private String date;
 
    public String getCurrencyCode() {
        return currencyCode;
    }
    
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public Double getRate() {
        return rate;
    }
    
    public void setRate(Double rate) {
        this.rate = rate;
    }
}
