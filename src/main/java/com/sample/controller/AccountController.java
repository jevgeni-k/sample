package com.sample.controller;

import com.sample.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

  private final AccountService accountService;

  @PostMapping("/add")
  public ResponseEntity<String> add(
      @RequestParam("accountNumber") String accountNumber,
      @RequestParam("amount") BigDecimal amount,
      @RequestParam("currency") String currency
  ) {
    accountService.addMoney(accountNumber, currency, amount);
    return ResponseEntity.ok("Money added successfully");
  }

  @PostMapping("/debit")
  public ResponseEntity<String> debit(
      @RequestParam("accountNumber") String accountNumber,
      @RequestParam("amount") BigDecimal amount,
      @RequestParam("currency") String currency
  ) {
    accountService.debitMoney(accountNumber, currency, amount);
    return ResponseEntity.ok("Money debited successfully");
  }

  @PostMapping("/exchange")
  public ResponseEntity<String> exchange(
      @RequestParam("accountNumber") String accountNumber,
      @RequestParam("amount") BigDecimal amount,
      @RequestParam("currency") String currency,
      @RequestParam("targetCurrency") String targetCurrency
  ) {
    accountService.exchange(accountNumber, currency, targetCurrency, amount);
    return ResponseEntity.ok("Money exchanged successfully");
  }

  @GetMapping("/balance")
  public ResponseEntity<BigDecimal> getBalance(
      @RequestParam("accountNumber") String accountNumber,
      @RequestParam("currency") String currency
  ) {
    BigDecimal amount = accountService.getBalance(accountNumber, currency);
    return ResponseEntity.ok(amount);
  }

}
