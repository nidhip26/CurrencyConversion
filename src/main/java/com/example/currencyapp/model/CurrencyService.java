package com.example.currencyapp.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CurrencyService {

    private static final Logger logger = LoggerFactory.getLogger(CurrencyService.class);

    private static final String API = "https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@%s/v1/currencies/usd.json";
    private static final Gson gson = new Gson();

    @Autowired
    private CurrencyRateRepository currencyRateRepository;

    public HashMap<String, Double> getRates() {
        String todayDate = getCurrentDate();
        logger.info("Fetching currency rates for date: {}", todayDate);

        HashMap<String, Double> storedRates = fetchRatesFromDatabase(todayDate);
        if (storedRates == null || storedRates.isEmpty()) {
            logger.info("No cached rates found for {}. Fetching from API.", todayDate);
            storedRates = fetchAndStoreRatesInDatabase(todayDate);
        } else {
            logger.info("Loaded {} currency rates from database.", storedRates.size());
        }

        return storedRates;
    }

    public double calculateRate(String fromCurrency, String toCurrency) {
        logger.info("Calculating rate from {} to {}", fromCurrency, toCurrency);
        HashMap<String, Double> rates = getRates();

        if ("usd".equals(fromCurrency)) {
            return rates.get(toCurrency);
        }
        return rates.get(toCurrency) / rates.get(fromCurrency);
    }

    private HashMap<String, Double> fetchRatesFromDatabase(String date) {
        logger.debug("Querying database for rates on {}", date);
        HashMap<String, Double> rates = new HashMap<>();
        List<CurrencyRate> currencyRates = currencyRateRepository.findByDate(date);

        if (currencyRates.isEmpty()) {
            logger.debug("No rates found in database for {}", date);
            return null;
        }

        for (CurrencyRate currencyRate : currencyRates) {
            rates.put(currencyRate.getCurrencyCode().toLowerCase(), currencyRate.getRate());
        }

        return rates;
    }

    @Transactional
    private HashMap<String, Double> fetchAndStoreRatesInDatabase(String todayDate) {
        logger.info("Fetching rates from external API for date: {}", todayDate);
        HashMap<String, Double> currencyRates = new HashMap<>();

        try {
            String apiUrl = String.format(API, todayDate);
            logger.debug("Constructed API URL: {}", apiUrl);
            String jsonResponse = fetchJsonFromApi(apiUrl);

            JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
            JsonObject rates = jsonObject.getAsJsonObject("usd");

            if (rates == null) {
                logger.warn("No 'usd' object found in API response");
                return null;
            }

            currencyRates = gson.fromJson(rates, new TypeToken<HashMap<String, Double>>() {}.getType());

            List<CurrencyRate> existingRates = currencyRateRepository.findByDate(todayDate);
            if (!existingRates.isEmpty()) {
                logger.info("Existing rates found for {}. Deleting old rates before update.", todayDate);
                currencyRateRepository.deleteAll(existingRates);
            }

            storeRatesInDatabase(currencyRates, todayDate);
            logger.info("Stored {} new currency rates for {}", currencyRates.size(), todayDate);

        } catch (IOException e) {
            logger.error("Error fetching rates from API: {}", e.getMessage(), e);
        }

        return currencyRates;
    }

    private void storeRatesInDatabase(HashMap<String, Double> rates, String todayDate) {
        for (Map.Entry<String, Double> entry : rates.entrySet()) {
            CurrencyRate currencyRate = new CurrencyRate();
            currencyRate.setCurrencyCode(entry.getKey().toUpperCase());
            currencyRate.setRate(entry.getValue());
            currencyRate.setDate(todayDate);
            currencyRateRepository.save(currencyRate);
        }
    }

    private String fetchJsonFromApi(String apiUrl) throws IOException {
        logger.debug("Sending GET request to {}", apiUrl);
        StringBuilder result = new StringBuilder();
        URL url = new URL(apiUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }

        return result.toString();
    }

    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        return formatter.format(new Date());
    }
}

