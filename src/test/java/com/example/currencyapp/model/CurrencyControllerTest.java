package com.example.currencyapp.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CurrencyController.class)
public class CurrencyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CurrencyService currencyService;

    @Autowired
    private ObjectMapper objectMapper;

    private HashMap<String, Double> mockRates;

    @BeforeEach
    void setUp() {
        mockRates = new HashMap<>();
        mockRates.put("usd", 1.0);
        mockRates.put("eur", 0.85);
        mockRates.put("gbp", 0.75);
        mockRates.put("jpy", 110.0);
        
        // mock service
        when(currencyService.getRates()).thenReturn(mockRates);
        
        // correct currencies 
        when(currencyService.calculateRate("usd", "eur")).thenReturn(0.85);
        when(currencyService.calculateRate("eur", "usd")).thenReturn(1.18);
        when(currencyService.calculateRate("gbp", "jpy")).thenReturn(146.67);
        
        // fake currencies
        when(currencyService.calculateRate("xyz", "usd")).thenReturn(-1.0);
        when(currencyService.calculateRate("usd", "xyz")).thenReturn(-1.0);
    }

    @Test
    void getCurrencies_ShouldReturnAllCurrencyRates() throws Exception {
        mockMvc.perform(get("/currencies"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.usd").value(1.0))
                .andExpect(jsonPath("$.eur").value(0.85))
                .andExpect(jsonPath("$.gbp").value(0.75))
                .andExpect(jsonPath("$.jpy").value(110.0));
    }

    @Test
    void getExchangeRate_WithValidCurrencyPair_ShouldReturnExchangeRate() throws Exception {
        CurrencyRequest request = new CurrencyRequest();
        request.setFromCurrency("USD");
        request.setToCurrency("EUR");
        
        mockMvc.perform(post("/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fromCurrency").value("usd"))
                .andExpect(jsonPath("$.toCurrency").value("eur"))
                .andExpect(jsonPath("$.rate").value(0.85));
    }

    // error no FromCurrency
    @Test
    void getExchangeRate_WithMissingFromCurrency_ShouldReturnBadRequest() throws Exception {
        CurrencyRequest request = new CurrencyRequest();
        request.setToCurrency("EUR");
        
        mockMvc.perform(post("/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Both 'fromCurrency' and 'toCurrency' are required."));
    }

    //error no ToCurrency
    @Test
    void getExchangeRate_WithMissingToCurrency_ShouldReturnBadRequest() throws Exception {
        CurrencyRequest request = new CurrencyRequest();
        request.setFromCurrency("USD");
        
        mockMvc.perform(post("/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Both 'fromCurrency' and 'toCurrency' are required."));
    }

    //error inccorect currency
    @Test
    void getExchangeRate_WithInvalidCurrencyPair_ShouldReturnBadRequest() throws Exception {
        CurrencyRequest request = new CurrencyRequest();
        request.setFromCurrency("USD");
        request.setToCurrency("XYZ");
        
        mockMvc.perform(post("/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("Invalid currency pair: 'usd' to 'xyz'."));
    }

    // success doesn't care about case
    @Test
    void getExchangeRate_WithCaseSensitivity_ShouldIgnoreCase() throws Exception {
        CurrencyRequest request = new CurrencyRequest();
        request.setFromCurrency("usd");  // lowercase
        request.setToCurrency("EUR");    // uppercase
        
        mockMvc.perform(post("/currencies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.fromCurrency").value("usd"))
                .andExpect(jsonPath("$.toCurrency").value("eur"))
                .andExpect(jsonPath("$.rate").value(0.85));
    }
}

