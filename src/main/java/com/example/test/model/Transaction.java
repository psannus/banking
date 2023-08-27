package com.example.test.model;

import com.example.test.model.enums.Currency;
import com.example.test.model.enums.TransactionDirection;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Transaction {
    Long id;
    Long accountId;
    BigDecimal amount;
    Currency currency;
    TransactionDirection direction;
    String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    BigDecimal balanceAfterTransaction;
}
