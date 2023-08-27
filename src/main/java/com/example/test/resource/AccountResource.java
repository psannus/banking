package com.example.test.resource;

import com.example.test.model.Account;
import com.example.test.model.Transaction;
import com.example.test.request.CreateAccountRequest;
import com.example.test.request.CreateTransactionRequest;
import com.example.test.response.AbstractResponse;
import com.example.test.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AccountResource {

    @Autowired
    private AccountService accountService;

    @PostMapping("/account")
    public AbstractResponse<Account> createAccount(@RequestBody CreateAccountRequest request) {
        return new AbstractResponse<>(HttpStatus.OK, "Successfully created account",
                accountService.createAccount(request));
    }

    @GetMapping("/account")
    public AbstractResponse<Account> getAccount(@RequestParam Long id) {
        return new AbstractResponse<>(HttpStatus.OK, "Successfully retrieved account",
                accountService.getAccount(id));
    }

    @PostMapping("/transaction")
    public AbstractResponse<Transaction> createTransaction(@RequestBody CreateTransactionRequest request) {
        return new AbstractResponse<>(HttpStatus.OK, "Successfully created transaction",
                accountService.createTransaction(request));
    }

    @GetMapping("/transaction")
    public AbstractResponse<List<Transaction>> getTransaction(@RequestParam Long accountId) {
        return new AbstractResponse<>(HttpStatus.OK, "Successfully retrieved transactions",
                accountService.getTransactionsForAccount(accountId));
    }
}
