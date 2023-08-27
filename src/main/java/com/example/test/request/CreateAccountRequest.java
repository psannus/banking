package com.example.test.request;

import com.example.test.model.enums.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateAccountRequest {
    Long customerId;
    String countryCode;
    List<Currency> currencies;
}
