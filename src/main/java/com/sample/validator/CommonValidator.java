package com.sample.validator;

import com.sample.exception.ValidationException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CommonValidator {

  public void validateAccountNumber(String accountNumber) {
    if (StringUtils.isBlank(accountNumber)) {
      throw new ValidationException("empty account number");
    }
  }

  public void validateAmount(BigDecimal amount) {
    if (amount.compareTo(BigDecimal.ZERO) < 0) {
      throw new ValidationException("cannot use negative amount");
    }
  }

}

