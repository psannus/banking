package com.example.test;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.annotation.AfterTestMethod;
import org.springframework.test.web.servlet.MockMvc;

import static com.example.test.util.TestUtils.getRandomCustomerId;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class AccountTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createAccount_withNullBalances() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": 11,
                    "countryCode": "US",
                    "balances": null
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid currencies",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void createAccount_withZeroBalances() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": 11,
                    "countryCode": "US",
                    "balances": []
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid currencies",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void createAccount_withOneBalance_thenDuplicateCustomerIdError() throws Exception {
        Long customerIdDuplicate = getRandomCustomerId();

        System.out.println("customerIdDuplicate: " + customerIdDuplicate);

        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": %d,
                    "countryCode": "US",
                    "currencies": [ "EUR" ]
                }
            """.formatted(customerIdDuplicate).trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "status": "OK",
                    "message": "Successfully created account",
                    "data": {
                        "customerId": %d,
                        "countryCode": "US",
                        "balances": [
                            {
                                "currency": "EUR",
                                "amount": 0.00
                            }
                        ]
                    }
                }
            """.formatted(customerIdDuplicate).trim()));

        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": %d,
                    "countryCode": "US",
                    "currencies": [ "EUR", "GBP" ]
                }
            """.formatted(customerIdDuplicate).trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value(containsString("duplicate key value violates unique constraint \"account_customer_id_key\"")));
    }

    @Test
    void createAccount_withTwoBalance_balanceDuplicateError() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": 12,
                    "countryCode": "US",
                    "currencies": [ "EUR", "EUR" ]
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid currencies",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void createAccount_withTwoBalance_oneFalseError() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": 12,
                    "countryCode": "US",
                    "currencies": [ "EUR", "EEK" ]
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
            .andExpect(jsonPath("$.message").value(containsString("Cannot deserialize value")));
    }

    @Test
    void createAccount_withTwoBalances() throws Exception {
        Long customerId = getRandomCustomerId();

        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": %d,
                    "countryCode": "US",
                    "currencies": [ "EUR", "GBP" ]
                }
            """.formatted(customerId).trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "status": "OK",
                    "message": "Successfully created account",
                    "data": {
                        "customerId": %d,
                        "countryCode": "US",
                        "balances": [
                            {
                                "currency": "EUR",
                                "amount": 0.00
                            },
                            {
                                "currency": "GBP",
                                "amount": 0.00
                            }
                        ]
                    }
                }
            """.formatted(customerId).trim()));
    }
    
    @Test
    void createAccount_withAllBalances() throws Exception {
        Long customerId = getRandomCustomerId();

        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": %d,
                    "countryCode": "US",
                    "currencies": [ "EUR", "GBP", "SEK", "USD" ]
                }
            """.formatted(customerId).trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "status": "OK",
                    "message": "Successfully created account",
                    "data": {
                        "customerId": %d,
                        "countryCode": "US",
                        "balances": [
                            {
                                "currency": "EUR",
                                "amount": 0.00
                            },
                            {
                                "currency": "GBP",
                                "amount": 0.00
                            },
                            {
                                "currency": "SEK",
                                "amount": 0.00
                            },
                            {
                                "currency": "USD",
                                "amount": 0.00
                            }
                        ]
                    }
                }
            """.formatted(customerId).trim()));
    }

    @Test
    void createAccount_withInvalidCountry() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": 14,
                    "countryCode": "USS",
                    "currencies": [ "EUR", "GBP" ]
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid countryCode",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void createAccount_withInvalidCountry2() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": 14,
                    "countryCode": "U ",
                    "currencies": [ "EUR", "GBP" ]
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid countryCode",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void createAccount_withInvalidCountry3() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": 14,
                    "countryCode": "  ",
                    "currencies": [ "EUR", "GBP" ]
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid countryCode",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void createAccount_withInvalidCountry4() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": 14,
                    "countryCode": null,
                    "currencies": [ "EUR", "GBP" ]
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid countryCode",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void createAccount_withInvalidCustomerId() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": -34,
                    "countryCode": "GB",
                    "currencies": [ "EUR", "GBP" ]
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid customerId",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void createAccount_withInvalidCustomerId2() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": 0,
                    "countryCode": "GB",
                    "currencies": [ "EUR", "GBP" ]
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid customerId",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void createAccount_withInvalidCustomerId3() throws Exception {
        mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": null,
                    "countryCode": "GB",
                    "currencies": [ "EUR", "GBP" ]
                }
            """.trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid customerId",
                    "data": null
                }
            """.trim()));
    }
    
    @Test
    void getAccount_successfullyCreated() throws Exception {
        Long customerId = getRandomCustomerId();

        String content =  mockMvc.perform(post("/account")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "customerId": %d,
                    "countryCode": "EE",
                    "currencies": [ "EUR" ]
                }
            """.formatted(customerId).trim())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "status": "OK",
                    "message": "Successfully created account",
                    "data": {
                        "customerId": %d,
                        "countryCode": "EE",
                        "balances": [
                            {
                                "currency": "EUR",
                                "amount": 0.00
                            }
                        ]
                    }
                }
            """.formatted(customerId).trim()))
            .andReturn().getResponse().getContentAsString();

        Long accountId = JsonPath.parse(content).read("$.data.id", Long.class);

        mockMvc.perform(get("/account?id=%d".formatted(accountId))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                    "status": "OK",
                    "message": "Successfully retrieved account",
                    "data": {
                        "customerId": %d,
                        "countryCode": "EE",
                        "balances": [
                            {
                                "currency": "EUR",
                                "amount": 0.00
                            }
                        ]
                    }
                }
            """.formatted(customerId).trim()));
    }

    @Test
    void getAccount_withInvalidId() throws Exception {
        mockMvc.perform(get("/account?id=0")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid id",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void getAccount_withInvalidId2() throws Exception {
        mockMvc.perform(get("/account?id=null")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "data": null
                }
            """.trim()))
            .andExpect(jsonPath("$.message").value(containsString("Failed to convert value")));
    }

    @Test
    void getAccount_withInvalidId3() throws Exception {
        mockMvc.perform(get("/account?id=-35")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "message": "Invalid id",
                    "data": null
                }
            """.trim()));
    }

    @Test
    void getAccount_invalidRequest() throws Exception {
        mockMvc.perform(get("/account")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("""
                {
                    "status": "BAD_REQUEST",
                    "data": null
                }
            """.trim()))
            .andExpect(jsonPath("$.message").value(containsString("Required request parameter 'id'")));
    }

    @Test
    void getAccount_validRequest_accountNotFoundError() throws Exception {
        mockMvc.perform(get("/account?id=56454949")
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(content().json("""
                {
                    "status": "NOT_FOUND",
                    "message": "Account not found",
                    "data": null
                }
            """.trim()));
    }
}
