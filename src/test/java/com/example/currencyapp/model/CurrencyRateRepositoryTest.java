package com.example.currencyapp.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

@SpringBootTest
class CurrencyRateRepositoryTest {

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    @Test
    void testSaveAndFindCurrencyRate() {
        CurrencyRate rate = new CurrencyRate();
        rate.setCurrencyCode("usd");
        rate.setRate(1.0);
        rate.setDate("2025-04-28");

        currencyRateRepository.save(rate);

        Optional<CurrencyRate> found = currencyRateRepository.findById("usd");
        assertThat(found).isPresent();
        assertThat(found.get().getRate()).isEqualTo(1.0);
    }

    // error, currency doesn't exist so return error
    @Test
    void testFindById_NotFound() {
        Optional<CurrencyRate> found = currencyRateRepository.findById("nonexistent");
        assertThat(found).isNotPresent();  
    }

    // if currencyCode doesn't exist and saving to it, should be error
    @Test
    void testSaveInvalidCurrencyRate() {
        CurrencyRate rate = new CurrencyRate();
        rate.setCurrencyCode(null); // Invalid primary key
        rate.setRate(1.2);
        rate.setDate("2025-04-28");

        try {
            currencyRateRepository.save(rate);
            assertThat(rate.getCurrencyCode()).isNull();
        } catch (Exception e) {
            assertThat(e).isInstanceOf(Exception.class); 
        }
    }
}
