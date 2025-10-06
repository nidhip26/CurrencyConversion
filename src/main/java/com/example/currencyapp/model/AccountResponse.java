package com.example.currencyapp.model;


import java.util.HashMap;
import java.util.Map;

public class AccountResponse {
    private String status;
    private String message;
    private Map<String, Object> data;
    
    public AccountResponse(String status, String message) {
        this.status = status;
        this.message = message;
        this.data = new HashMap<>();
    }
    
    public void addData(String key, Object value) {
        this.data.put(key, value);
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Map<String, Object> getData() {
        return data;
    }
    
    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}