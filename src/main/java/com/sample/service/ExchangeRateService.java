package com.sample.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

  Map<String, BigDecimal> WEIGHTS = Map.of(
      "EUR", BigDecimal.ONE,
      "USD", new BigDecimal("1.2"),
      "SEK", new BigDecimal(4),
      "RUB", new BigDecimal(100)
  );

  public BigDecimal getRate(String sourceCurrency, String targetCurrency) {
    BigDecimal wSrc = WEIGHTS.get(sourceCurrency.toUpperCase());
    BigDecimal wTrg = WEIGHTS.get(targetCurrency.toUpperCase());

    BigDecimal rate = wSrc.divide(wTrg, new MathContext(10,RoundingMode.DOWN));
    System.out.printf("Exchange rate %s to %s is %s ", sourceCurrency, targetCurrency, rate.toString());
    return rate;
  }
}
