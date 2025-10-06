package com.example.currencyapp.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/currencies")
public class CurrencyController {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyController.class);

    @Autowired
    private CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<?> getCurrencies() {
        logger.info("Request to get all currencies rates.");
        HashMap<String, Double> rates = currencyService.getRates();
        logger.info("Returning currency rates: {}", rates);
        return ResponseEntity.ok(rates);
    }

    @PostMapping
    public ResponseEntity<?> getExchangeRate(@RequestBody CurrencyRequest request) {
        if (request.getFromCurrency() == null || request.getToCurrency() == null) {
            logger.error("Error: 'fromCurrency' or 'toCurrency' is missing in the request.");
            HashMap<String, String> error = new HashMap<>();
            error.put("error", "Both 'fromCurrency' and 'toCurrency' are required.");
            return ResponseEntity.badRequest().body(error);
        }

        String fromCurrency = request.getFromCurrency().toLowerCase();
        String toCurrency = request.getToCurrency().toLowerCase();
        logger.info("Calculating exchange rate from {} to {}", fromCurrency, toCurrency);

        double rate = currencyService.calculateRate(fromCurrency, toCurrency);
        if (rate < 0) {
            logger.error("Invalid currency pair: '{}' to '{}'.", fromCurrency, toCurrency);
            HashMap<String, String> error = new HashMap<>();
            error.put("error", "Invalid currency pair: '" + fromCurrency + "' to '" + toCurrency + "'.");
            return ResponseEntity.badRequest().body(error);
        }

        CurrencyResponse response = new CurrencyResponse(fromCurrency, toCurrency, rate);
        logger.info("Exchange rate from {} to {} is: {}", fromCurrency, toCurrency, rate);
        return ResponseEntity.ok(response);
    }

}


