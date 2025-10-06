package com.example.currencyapp.model;


import java.io.Serializable;
import java.util.Objects;

public class UserAccountId implements Serializable {
    
    private String username;
    private String currencyCode;
    
    public UserAccountId() {}
    
    public UserAccountId(String username, String currencyCode) {
        this.username = username;
        this.currencyCode = currencyCode;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccountId that = (UserAccountId) o;
        return Objects.equals(username, that.username) &&
                Objects.equals(currencyCode, that.currencyCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(username, currencyCode);
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
}
