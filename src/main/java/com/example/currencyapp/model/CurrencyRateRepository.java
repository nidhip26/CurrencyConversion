package com.example.currencyapp.model;

import com.example.currencyapp.model.CurrencyRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CurrencyRateRepository extends JpaRepository<CurrencyRate, String> {
    
    @Query("SELECT c FROM CurrencyRate c WHERE c.date = :date")
    List<CurrencyRate> findByDate(@Param("date") String date);
}

