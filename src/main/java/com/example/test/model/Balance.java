package com.example.test.model;

import com.example.test.model.enums.Currency;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class Balance {
    @JsonIgnore Long id;
    @JsonIgnore Long accountId;
    Currency currency;
    BigDecimal amount;
}
