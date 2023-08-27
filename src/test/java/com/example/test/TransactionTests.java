package com.example.test;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;

import static com.example.test.util.TestUtils.getRandomCustomerId;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionTests {

    @Autowired
    private MockMvc mockMvc;

    private static Long acc1Id = null;
    private static Long acc2Id = null;

    @BeforeAll
    void setup() throws Exception {
        Long customerId1 = getRandomCustomerId();
        Long customerId2 = getRandomCustomerId();

        String resp1 = mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": %d,
                    "countryCode": "BR",
                    "currencies": ["SEK"]
                }
            """.formatted(customerId1).trim()))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "status": "OK",
                    "message": "Successfully created account"
                }
            """.trim())).andReturn().getResponse().getContentAsString();

        acc1Id = JsonPath.parse(resp1).read("$.data.id", Long.class);

        String resp2 = mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": %d,
                    "countryCode": "RU",
                    "currencies": ["USD"]
                }
            """.formatted(customerId2).trim()))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "status": "OK",
                    "message": "Successfully created account"
                }
            """.trim())).andReturn().getResponse().getContentAsString();

        acc2Id = JsonPath.parse(resp2).read("$.data.id", Long.class);
    }

    @Test
    void createTransaction_withValidRequest() throws Exception {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "accountId": %d,
                    "amount": 100.12,
                    "currency": "SEK",
                    "direction": "IN",
                    "description": "Test transaction"
                }
            """.formatted(acc1Id)
            .trim()))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "status": "OK",
                    "message": "Successfully created transaction",
                    "data": {
                        "accountId": %d,
                        "amount": 100.12,
                        "currency": "SEK",
                        "direction": "IN",
                        "description": "Test transaction"
                    }
                }
            """.formatted(acc1Id).trim()));
    }

    @Test
    void getTransactions_valid() throws Exception {
        mockMvc.perform(get("/transaction?accountId=%d".formatted(acc1Id))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "status": "OK",
                    "message": "Successfully retrieved transactions",
                    "data": [
                        {
                            "accountId": %d,
                            "amount": 100.12,
                            "currency": "SEK",
                            "direction": "IN",
                            "description": "Test transaction"
                        }
                    ]
                }
            """.formatted(acc1Id).trim()));
    }

    @Test
    void createTransaction_withValidRequest_x100() throws Exception {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal lastBalance = BigDecimal.ZERO;

        for (int i = 1000; i <= 1100; i++) {
            ResultActions content = mockMvc.perform(post("/transaction")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                        {
                                            "accountId": %d,
                                            "amount": 100.12,
                                            "currency": "USD",
                                            "direction": "IN",
                                            "description": "Test transaction #%s"
                                        }
                                    """.formatted(acc2Id, i)
                                    .trim()))
                    .andExpect(status().isOk())
                    .andExpect(content().json("""
                                {
                                    "status": "OK",
                                    "message": "Successfully created transaction",
                                    "data": {
                                        "accountId": %d,
                                        "amount": 100.12,
                                        "currency": "USD",
                                        "direction": "IN",
                                        "description": "Test transaction #%s"
                                    }
                                }
                            """.formatted(acc2Id, i).trim()));
            if (i == 1100) {
                lastBalance = JsonPath.parse(content.andReturn().getResponse().getContentAsString())
                        .read("$.data.balanceAfterTransaction", BigDecimal.class);
            }

            total = total.add(BigDecimal.valueOf(100.12));
        }

        assertEquals(total, lastBalance);

        mockMvc.perform(get("/account?id=%d".formatted(acc2Id))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.balances[0].amount").value(total));

        mockMvc.perform(get("/transaction?accountId=%d".formatted(acc2Id))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(101));
    }

    @Test
    void createTransaction_invalidAccountId() throws Exception {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "accountId": 999999999,
                    "amount": 100.12,
                    "currency": "SEK",
                    "direction": "IN",
                    "description": "Test transaction"
                }
            """.trim()))
            .andExpect(status().isNotFound())
            .andExpect(content().json("""
                {
                    "status": "NOT_FOUND",
                    "message": "Account not found"
                }
            """.trim()));
    }

    @Test
    void createTransaction_invalidAmount() throws Exception {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "accountId": %d,
                    "amount": -100.12,
                    "currency": "SEK",
                    "direction": "IN",
                    "description": "Test transaction"
                }
            """.formatted(acc1Id)
            .trim()))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid amount"
                }
            """.trim()));
    }

    @Test
    void createTransaction_invalidCurrency() throws Exception {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "accountId": %d,
                    "amount": 100.12,
                    "currency": "RUB",
                    "direction": "IN",
                    "description": "Test transaction"
                }
            """.formatted(acc1Id)
            .trim()))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST"
                }
            """.trim()))
            .andExpect(jsonPath("$.message").value(containsString("Cannot deserialize value")));
    }

    @Test
    void createTransaction_invalidDirection() throws Exception {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "accountId": %d,
                    "amount": 100.12,
                    "currency": "SEK",
                    "direction": "LEFT",
                    "description": "Test transaction"
                }
            """.formatted(acc1Id)
            .trim()))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST"
                }
            """.trim()))
            .andExpect(jsonPath("$.message").value(containsString("Cannot deserialize value")));
    }

    @Test
    void createTransaction_invalidDescription() throws Exception {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "accountId": %d,
                    "amount": 100.12,
                    "currency": "SEK",
                    "direction": "IN",
                    "description": ""
                }
            """.formatted(acc1Id)
            .trim()))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid description"
                }
            """.trim()));
    }

    @Test
    void createTransaction_insufficientFundsError() throws Exception {
        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "accountId": %d,
                    "amount": 200.24,
                    "currency": "SEK",
                    "direction": "OUT",
                    "description": "Too big of a transaction"
                }
            """.formatted(acc1Id)
                                .trim()))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Not enough balance"
                }
            """.trim()));
    }

    @Test
    void createTransaction_noCorrectCurrencyBalanceError() throws Exception {
        mockMvc.perform(post("/transaction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                {
                    "accountId": %d,
                    "amount": 200.24,
                    "currency": "GBP",
                    "direction": "OUT",
                    "description": "I don't own any this currency, sadly..."
                }
            """.formatted(acc1Id)
                                .trim()))
                .andExpect(status().isBadRequest())
                .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "No balance in required currency"
                }
            """.trim()));
    }

    @Test
    void getTransactions_validAccountId_accountNotFoundError() throws Exception {
        mockMvc.perform(get("/transaction?accountId=9999")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().json("""
                {
                    "status": "NOT_FOUND",
                    "message": "Account not found"
                }
            """.trim()));
    }

    @Test
    void getTransactions_validAccountId_transactionsNotFoundError() throws Exception {
        Long customerId = getRandomCustomerId();

        String response = mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": %d,
                    "countryCode": "NO",
                    "currencies": [ "USD" ]
                }
            """.formatted(customerId).trim()))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        Long accountId = JsonPath.parse(response).read("$.data.id", Long.class);

        mockMvc.perform(get("/transaction?accountId=%d".formatted(accountId))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().json("""
                {
                    "status": "NOT_FOUND",
                    "message": "Transaction(s) not found"
                }
            """.trim()));
    }

    @Test
    void createTransaction_validSubtractRequest() throws Exception {
        mockMvc.perform(post("/transaction")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "accountId": %d,
                    "amount": 100.00,
                    "currency": "SEK",
                    "direction": "OUT",
                    "description": "(Almost) too big of a transaction"
                }
            """.formatted(acc1Id).trim()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.balanceAfterTransaction").value("0.12"));
    }
}
