package com.example.test.request;

import com.example.test.model.enums.Currency;
import com.example.test.model.enums.TransactionDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class CreateTransactionRequest {
    Long accountId;
    BigDecimal amount;
    Currency currency;
    TransactionDirection direction;
    String description;
}
