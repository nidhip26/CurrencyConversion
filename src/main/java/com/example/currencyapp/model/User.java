package com.example.currencyapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @Column(name = "username")
    private String username;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    public User() {
    }
    
    public User(String username) {
        this.username = username;
        this.createdAt = LocalDateTime.now();
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
