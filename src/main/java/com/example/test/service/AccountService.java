package com.example.test.service;

import com.example.test.data.AccountMapper;
import com.example.test.data.TransactionMapper;
import com.example.test.exception.InsufficientFundsException;
import com.example.test.exception.NotFoundException;
import com.example.test.model.Account;
import com.example.test.model.Balance;
import com.example.test.model.Transaction;
import com.example.test.model.enums.Currency;
import com.example.test.model.enums.TransactionDirection;
import com.example.test.rabbitmq.RabbitMQProducer;
import com.example.test.request.CreateAccountRequest;
import com.example.test.request.CreateTransactionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RabbitMQProducer producer;

    public AccountService(RabbitMQProducer producer) {
        this.producer = producer;
    }

    public Account getAccount(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid id");
        }

        Account account = accountMapper.getAccountById(id);
        if (account == null) {
            throw new NotFoundException("Account not found");
        }

        return account;
    }

    public Account createAccount(CreateAccountRequest request) {
        if (request.getCustomerId() == null || request.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Invalid customerId");
        }
        if (request.getCountryCode() == null || request.getCountryCode().isBlank()
                || request.getCountryCode().length() != 2 || request.getCountryCode().trim().length() != 2) {
            throw new IllegalArgumentException("Invalid countryCode");
        }
        HashSet<Currency> availableCurrencies = new HashSet<>(Arrays.asList(Currency.values()));
        if (request.getCurrencies() == null || !availableCurrencies.containsAll(request.getCurrencies())
            || request.getCurrencies().stream().distinct().toList().size() != request.getCurrencies().size()) {
            throw new IllegalArgumentException("Invalid currencies");
        }

        Account account = new Account();
        account.setCustomerId(request.getCustomerId());
        account.setCountryCode(request.getCountryCode());

        Boolean accountCreated = accountMapper.insertAccount(account);
        if (!accountCreated) {
            throw new RuntimeException("Failed to create account");
        } else {
            try {
                producer.produce(objectMapper.writeValueAsString(account));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        // mybatis seems to not support nested inserts, so we have to insert balances separately
        Long accountId = account.getId();

        request.getCurrencies().forEach( it -> {
            Balance balance = new Balance();
            balance.setCurrency(it);
            balance.setAmount(BigDecimal.ZERO);
            balance.setAccountId(accountId);

            Boolean balanceCreated = accountMapper.insertBalance(balance);
            if (!balanceCreated) {
                throw new RuntimeException("Failed to create balance");
            } else {
                try {
                    producer.produce(objectMapper.writeValueAsString(balance));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // refreshing account object to display the added balances
        account = accountMapper.getAccountById(accountId);

        return account;
    }

    public Transaction createTransaction(CreateTransactionRequest request) {
        if (request.getAccountId() == null || request.getAccountId() <= 0) {
            throw new IllegalArgumentException("Invalid accountId");
        }
        HashSet<Currency> availableCurrencies = new HashSet<>(Arrays.asList(Currency.values()));
        if (request.getCurrency() == null || !availableCurrencies.contains(request.getCurrency())) {
            throw new IllegalArgumentException("Invalid currency");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0
                || request.getAmount().scale() > 2) {
            throw new IllegalArgumentException("Invalid amount");
        }
        HashSet<TransactionDirection> availableDirections = new HashSet<>(Arrays.asList(TransactionDirection.values()));
        if (request.getDirection() == null || !availableDirections.contains(request.getDirection())) {
            throw new IllegalArgumentException("Invalid direction");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) { // "description is missing"
            throw new IllegalArgumentException("Invalid description");
        }

        Account account = accountMapper.getAccountById(request.getAccountId());
        if (account == null) {
            throw new NotFoundException("Account not found");
        }

        Optional<Balance> balanceOptional = account.getBalances().stream()
                .filter(it -> it.getCurrency() == request.getCurrency()).findFirst();
        if (balanceOptional.isPresent()) {
            Balance currencyBalance = balanceOptional.get();
            if (request.getDirection().equals(TransactionDirection.OUT) && request.getAmount().compareTo(currencyBalance.getAmount()) > 0) {
                throw new InsufficientFundsException("Not enough balance");
            }

            // update balance with new amount
            BigDecimal newBalanceAmount = request.getDirection().equals(TransactionDirection.IN)
                    ? currencyBalance.getAmount().add(request.getAmount())
                    : currencyBalance.getAmount().subtract(request.getAmount());

            currencyBalance.setAmount(newBalanceAmount);
            Boolean balanceUpdated = accountMapper.updateBalance(currencyBalance);
            if (!balanceUpdated) {
                throw new RuntimeException("Failed to update balance");
            } else {
                try {
                    producer.produce(objectMapper.writeValueAsString(currencyBalance));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            Transaction transaction = new Transaction();
            transaction.setAccountId(request.getAccountId());
            transaction.setAmount(request.getAmount());
            transaction.setCurrency(request.getCurrency());
            transaction.setDirection(request.getDirection());
            transaction.setDescription(request.getDescription());

            Boolean transactionCreated = transactionMapper.insertTransaction(transaction);
            if (!transactionCreated) {
                throw new RuntimeException("Failed to create transaction");
            } else {
                try {
                    producer.produce(objectMapper.writeValueAsString(transaction));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            transaction.setBalanceAfterTransaction(newBalanceAmount);

            return transaction;
        } else {
            throw new InsufficientFundsException("No balance in required currency");
            // here we could create a new balance for account if direction is IN, and complete transaction...
            // BUT if we could freely create new balances, we could then just init account
            // with all possible balances straight away?
        }
    }

    public List<Transaction> getTransactionsForAccount(Long accountId) {
        if (accountId == null || accountId <= 0) {
            throw new IllegalArgumentException("Invalid accountId");
        }

        Account account = accountMapper.getAccountById(accountId);
        if (account == null) {
            throw new NotFoundException("Account not found");
        }

        List<Transaction> transaction = transactionMapper.getTransactionsByAccountId(accountId);
        if (transaction == null || transaction.isEmpty()) {
            throw new NotFoundException("Transaction(s) not found");
        }

        return transaction;
    }
}
