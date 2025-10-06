package com.example.currencyapp.model;


import com.example.currencyapp.model.UserAccount;
import com.example.currencyapp.model.UserAccountId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, UserAccountId> {
    List<UserAccount> findByUsername(String username);
    Optional<UserAccount> findByUsernameAndCurrencyCode(String username, String currencyCode);
}