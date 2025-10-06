package com.example.currencyapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;

@WebMvcTest(AccountController.class)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    private final String TEST_USERNAME = "testuser";
    private final String USD = "usd";
    private final String EUR = "eur";
    private final double AMOUNT = 100.0;

    @BeforeEach
    public void setUp() {
        
    }

    @Test
    public void testHandleAccountOperation_Deposit() throws Exception {
        // create response
        AccountResponse successResponse = new AccountResponse("success", "Deposit successful");
        successResponse.addData("username", TEST_USERNAME);
        successResponse.addData("currencyType", USD);
        successResponse.addData("balance", AMOUNT);

        when(accountService.handleDeposit(any(DepositRequest.class)))
                .thenReturn(ResponseEntity.ok(successResponse));

        // is deposit post working
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + TEST_USERNAME + "\",\"amount\":" + AMOUNT + ",\"deposit\":\"" + USD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Deposit successful")))
                .andExpect(jsonPath("$.data.username", is(TEST_USERNAME)))
                .andExpect(jsonPath("$.data.currencyType", is(USD)))
                .andExpect(jsonPath("$.data.balance", is(AMOUNT)));
    }

    @Test
    public void testHandleAccountOperation_DepositError() throws Exception {
        // make error response
        AccountResponse errorResponse = new AccountResponse("error", "Username not found");
        
        when(accountService.handleDeposit(any(DepositRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));

        // if the username does not exist, showuld return error
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nonexistent\",\"amount\":" + AMOUNT + ",\"deposit\":\"" + USD + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Username not found")));
    }

    @Test
    public void testHandleAccountOperation_Transfer() throws Exception {
        // create response for transfer
        AccountResponse successResponse = new AccountResponse("success", "Transfer successful");
        successResponse.addData("username", TEST_USERNAME);
        successResponse.addData("fromAccount", USD);
        successResponse.addData("toAccount", EUR);
        successResponse.addData("amountTransferred", 85.0);
        successResponse.addData("fromAccountBalance", 900.0);
        successResponse.addData("toAccountBalance", 1085.0);

        when(accountService.handleTransfer(any(TransferRequest.class)))
                .thenReturn(ResponseEntity.ok(successResponse));

        // test the post request working
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + TEST_USERNAME + "\",\"amount\":" + AMOUNT + 
                         ",\"fromAccount\":\"" + USD + "\",\"toAccount\":\"" + EUR + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Transfer successful")))
                .andExpect(jsonPath("$.data.username", is(TEST_USERNAME)))
                .andExpect(jsonPath("$.data.fromAccount", is(USD)))
                .andExpect(jsonPath("$.data.toAccount", is(EUR)))
                .andExpect(jsonPath("$.data.amountTransferred", is(85.0)))
                .andExpect(jsonPath("$.data.fromAccountBalance", is(900.0)))
                .andExpect(jsonPath("$.data.toAccountBalance", is(1085.0)));
    }

    @Test
    public void testHandleAccountOperation_TransferError() throws Exception {
        // error response for failed transfer
        AccountResponse errorResponse = new AccountResponse("error", "Insufficient funds or invalid fromAccount");
        
        when(accountService.handleTransfer(any(TransferRequest.class)))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));

        // error if low funds
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + TEST_USERNAME + "\",\"amount\":10000" + 
                         ",\"fromAccount\":\"" + USD + "\",\"toAccount\":\"" + EUR + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Insufficient funds or invalid fromAccount")));
    }

    @Test
    public void testHandleAccountOperation_InvalidRequest() throws Exception {
        // returns error if body doesn't have correct # of params
        mockMvc.perform(post("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + TEST_USERNAME + "\",\"amount\":" + AMOUNT + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Invalid request body")));
    }

    @Test
    public void testGetAccounts_Success() throws Exception {
        // response for getting accounts
        AccountResponse successResponse = new AccountResponse("success", "Accounts retrieved");
        Map<String, Double> accountMap = new HashMap<>();
        accountMap.put(USD, 1000.0);
        accountMap.put(EUR, 850.0);
        successResponse.addData("accounts", accountMap);

        when(accountService.getUserAccounts(TEST_USERNAME))
                .thenReturn(ResponseEntity.ok(successResponse));

        // tests if all accounts are returned successfully
        mockMvc.perform(get("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + TEST_USERNAME + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Accounts retrieved")))
                .andExpect(jsonPath("$.data.accounts." + USD, is(1000.0)))
                .andExpect(jsonPath("$.data.accounts." + EUR, is(850.0)));
    }

    @Test
    public void testGetAccounts_Error() throws Exception {
        // error response
        AccountResponse errorResponse = new AccountResponse("error", "Username not found");
        
        when(accountService.getUserAccounts("nonexistent"))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));

        // if username doesn't exist, should return error
        mockMvc.perform(get("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nonexistent\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Username not found")));
    }

    @Test
    public void testUpdateAccount_Success() throws Exception {
        // response for updating balance
        AccountResponse successResponse = new AccountResponse("success", "Account balance updated successfully");
        successResponse.addData("username", TEST_USERNAME);
        successResponse.addData("currencyType", USD);
        successResponse.addData("newBalance", AMOUNT);

        when(accountService.updateAccountBalance(eq(TEST_USERNAME), eq(USD), eq(AMOUNT)))
                .thenReturn(ResponseEntity.ok(successResponse));

        // tests PUT request
        mockMvc.perform(put("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + TEST_USERNAME + "\",\"account\":\"" + USD + "\",\"amount\":" + AMOUNT + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Account balance updated successfully")))
                .andExpect(jsonPath("$.data.username", is(TEST_USERNAME)))
                .andExpect(jsonPath("$.data.currencyType", is(USD)))
                .andExpect(jsonPath("$.data.newBalance", is(AMOUNT)));
    }

    @Test
    public void testUpdateAccount_Error() throws Exception {
        // error response
        AccountResponse errorResponse = new AccountResponse("error", "Username not found");
        
        when(accountService.updateAccountBalance(eq("nonexistent"), eq(USD), anyDouble()))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));

        // if incorrenct username -> error
        mockMvc.perform(put("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"nonexistent\",\"account\":\"" + USD + "\",\"amount\":" + AMOUNT + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Username not found")));
    }

    @Test
    public void testDeleteAccount_Success() throws Exception {
        // response for deleting account
        AccountResponse successResponse = new AccountResponse("success", "Currency type account deleted successfully");
        successResponse.addData("username", TEST_USERNAME);
        successResponse.addData("deletedAccount", USD);

        when(accountService.deleteAccount(eq(TEST_USERNAME), eq(USD)))
                .thenReturn(ResponseEntity.ok(successResponse));

        // tests DELETE request
        mockMvc.perform(delete("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + TEST_USERNAME + "\",\"delete\":\"" + USD + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("Currency type account deleted successfully")))
                .andExpect(jsonPath("$.data.username", is(TEST_USERNAME)))
                .andExpect(jsonPath("$.data.deletedAccount", is(USD)));
    }

    @Test
    public void testDeleteAccount_Error() throws Exception {
        // error response
        AccountResponse errorResponse = new AccountResponse("error", "Currency type account not found");
        
        when(accountService.deleteAccount(eq(TEST_USERNAME), eq("nonexistent")))
                .thenReturn(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse));

        // if account doesn't exit -> error
        mockMvc.perform(delete("/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + TEST_USERNAME + "\",\"delete\":\"nonexistent\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.message", is("Currency type account not found")));
    }
}
