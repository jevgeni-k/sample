package com.sample.service;

import com.sample.dbo.Account;
import com.sample.dbo.Balance;
import com.sample.exception.BusinessException;
import com.sample.exception.ValidationException;
import com.sample.repo.AccountRepository;
import com.sample.repo.BalanceRepository;
import com.sample.validator.CommonValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

  // FIXME fancier locking mechanism needed to support running on multiple nodes
  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private final AccountRepository accountRepository;
  private final BalanceRepository balanceRepository;
  private final ExchangeRateService exchangeRateService;
  private final CommonValidator commonValidator;
  private final RestTemplate restTemplate;

  public void addMoney(String accountNumber, String currency, BigDecimal amount) {
    commonValidator.validateAccountNumber(accountNumber);
    commonValidator.validateAmount(amount);

    lock.writeLock().lock();
    try {
      doAddMoney(accountNumber, currency, amount);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void debitMoney(String accountNumber, String currency, BigDecimal amount) {
    commonValidator.validateAccountNumber(accountNumber);
    commonValidator.validateAmount(amount);

    lock.writeLock().lock();
    try {
      doDebitMoney(accountNumber, currency, amount);
    } finally {
      lock.writeLock().unlock();
    }
  }

  public void exchange(String accountNumber, String currency, String targetCurrency, BigDecimal amount) {
    commonValidator.validateAccountNumber(accountNumber);
    commonValidator.validateAmount(amount);

    lock.writeLock().lock();
    try {
      BigDecimal sourceBalance = doGetBalance(accountNumber, currency);
      if (sourceBalance.compareTo(amount) < 0) {
        throw new BusinessException("insufficient funds of specified currency");
      }

      BigDecimal rate = exchangeRateService.getRate(currency, targetCurrency);

      doDebitMoney(accountNumber, currency, amount);
      doAddMoney(accountNumber, targetCurrency, amount.multiply(rate));
    } finally {
      lock.writeLock().unlock();
    }
  }

  public BigDecimal getBalance(String accountNumber, String currency) {
    commonValidator.validateAccountNumber(accountNumber);

    lock.readLock().lock();
    try {
      return doGetBalance(accountNumber, currency);
    } finally {
      lock.readLock().unlock();
    }
  }

  private BigDecimal doGetBalance(String accountNumber, String currency) {
    Account account = accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new BusinessException("account with specified number does not exist"));
    Optional<Balance> balance = balanceRepository.findByAccountAndCurrency(account, currency);
    return balance.map(Balance::getAmount)
        .orElse(BigDecimal.ZERO);
  }

  private void doAddMoney(String accountNumber, String currency, BigDecimal amount) {
    Account account = accountRepository.findByAccountNumber(accountNumber)
        .orElseGet(() -> {
          Account a = new Account();
          a.setAccountNumber(accountNumber);
          return accountRepository.save(a);
        });

    balanceRepository
        .findByAccountAndCurrency(account, currency)
        .ifPresentOrElse(b -> {
          b.setAmount(b.getAmount().add(amount));
          balanceRepository.save(b);
        }, () -> {
          Balance b = new Balance();
          b.setAccount(account);
          b.setAmount(amount);
          b.setCurrency(currency);
          balanceRepository.save(b);
        });
  }

  private void doDebitMoney(String accountNumber, String currency, BigDecimal amount) {
    Account account = accountRepository.findByAccountNumber(accountNumber)
        .orElseThrow(() -> new BusinessException("account with specified number does not exist"));

    Balance balance = balanceRepository.findByAccountAndCurrency(account, currency)
        .orElseThrow(() -> new BusinessException("there is no money of specified currency on this account"));

    if (balance.getAmount().compareTo(amount) < 0) {
      throw new ValidationException("insufficient funds");
    }

    doFakeLog(); // FIXME? currently invoked during both 'debit' and 'exchange' operations

    balance.setAmount(balance.getAmount().subtract(amount));
    balanceRepository.save(balance);
  }

  public void doFakeLog() {
    String result = restTemplate.getForObject("https://httpstat.us/200", String.class);
    System.out.printf("Response from external service: %s%n", result);
  }

}
