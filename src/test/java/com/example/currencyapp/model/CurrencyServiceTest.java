package com.example.currencyapp.model;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyServiceTest {

    @Mock
    private CurrencyRateRepository currencyRateRepository;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // testing getting rates
    @Test
    void testGetRates_WhenRatesExistInDatabase() {
        CurrencyRate rate = new CurrencyRate();
        rate.setCurrencyCode("EUR");
        rate.setRate(0.9);
        rate.setDate("2025-04-26");

        when(currencyRateRepository.findByDate(anyString()))
                .thenReturn(List.of(rate));

        HashMap<String, Double> rates = currencyService.getRates();

        assertNotNull(rates);
        assertEquals(0.9, rates.get("eur"));
    }

    //calculate rates from usd to another
    @Test
    void testCalculateRate_FromUsdToOther() {
        HashMap<String, Double> mockRates = new HashMap<>();
        mockRates.put("eur", 0.9);
        mockRates.put("usd", 1.0);

        CurrencyService spyService = spy(currencyService);
        doReturn(mockRates).when(spyService).getRates();

        double result = spyService.calculateRate("usd", "eur");
        assertEquals(0.9, result);
    }

    //calculates rates from another to another currency
    @Test
    void testCalculateRate_FromOtherToOther() {
        HashMap<String, Double> mockRates = new HashMap<>();
        mockRates.put("eur", 0.9);
        mockRates.put("gbp", 0.8);

        //using spy because don't want to use db connection for getRates
        CurrencyService spyService = spy(currencyService);
        doReturn(mockRates).when(spyService).getRates();

        double result = spyService.calculateRate("eur", "gbp");

        assertEquals(0.8 / 0.9, result, 0.0001);
    }

    //just making sure the api request actually fetches something
    @Test
    void testFetchRatesFromDatabase_WhenEmpty() {
        when(currencyRateRepository.findByDate(anyString()))
                .thenReturn(Collections.emptyList());

        HashMap<String, Double> rates = currencyService.getRates();

        assertNotNull(rates);
    }
}
