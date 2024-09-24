package com.sample.repo;

import com.sample.dbo.Account;
import com.sample.dbo.Balance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {

  Optional<Balance> findByAccountAndCurrency(Account account, String currency);

}
