package com.example.currencyapp.model;


import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UserAccountRepositoryTest {

    @Autowired
    private UserAccountRepository userAccountRepository;

    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        userAccount = new UserAccount("testuser", "usd", 100.0);
        userAccountRepository.save(userAccount);
    }


    @Test
    void findByUsername_shouldReturnAccounts() {
        // should return the usd account for testuser
        List<UserAccount> accounts = userAccountRepository.findByUsername("testuser");

        //checks if response is correct
        assertThat(accounts).isNotEmpty();
        assertThat(accounts.get(0).getUsername()).isEqualTo("testuser");
        assertThat(accounts.get(0).getCurrencyCode()).isEqualTo("usd");
        assertThat(accounts.get(0).getBalance()).isEqualTo(100.0);
    }

    @Test
    void findByUsernameAndCurrencyCode_shouldReturnCorrectAccount() {
        //should return usd account of user
        Optional<UserAccount> accountOpt = userAccountRepository.findByUsernameAndCurrencyCode("testuser", "usd");

        assertThat(accountOpt).isPresent();
        UserAccount account = accountOpt.get();
        assertThat(account.getUsername()).isEqualTo("testuser");
        assertThat(account.getCurrencyCode()).isEqualTo("usd");
        assertThat(account.getBalance()).isEqualTo(100.0);
    }

    //if user doesn't exist, shouldn't return anything
    @Test
    void findByUsernameAndCurrencyCode_shouldReturnEmptyForNonExisting() {
        Optional<UserAccount> accountOpt = userAccountRepository.findByUsernameAndCurrencyCode("unknown", "usd");

        assertThat(accountOpt).isNotPresent();
    }
}
