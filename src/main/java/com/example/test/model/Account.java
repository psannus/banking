package com.example.test.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class Account {
    Long id;
    Long customerId;
    String countryCode;
    List<Balance> balances;
}
