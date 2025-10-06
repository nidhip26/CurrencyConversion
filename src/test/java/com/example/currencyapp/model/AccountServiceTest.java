package com.example.currencyapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AccountServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private AccountService accountService;

    private final String TEST_USERNAME = "testuser";
    private final String USD = "usd";
    private final String EUR = "eur";
    private final double INITIAL_BALANCE = 1000.0;
    private final double TRANSFER_AMOUNT = 100.0;
    private final double CONVERSION_RATE = 0.85; // USD to EUR

    @BeforeEach
    public void setUp() {
       
        when(userRepository.existsById(TEST_USERNAME)).thenReturn(true);
        when(userRepository.existsById("nonexistent")).thenReturn(false);

        UserAccount usdAccount = new UserAccount(TEST_USERNAME, USD, INITIAL_BALANCE);
        UserAccount eurAccount = new UserAccount(TEST_USERNAME, EUR, INITIAL_BALANCE);
        
        when(userAccountRepository.findByUsernameAndCurrencyCode(TEST_USERNAME, USD))
            .thenReturn(Optional.of(usdAccount));
        when(userAccountRepository.findByUsernameAndCurrencyCode(TEST_USERNAME, EUR))
            .thenReturn(Optional.of(eurAccount));
        when(userAccountRepository.findByUsername(TEST_USERNAME))
            .thenReturn(Arrays.asList(usdAccount, eurAccount));
            
        when(currencyService.calculateRate(USD, EUR)).thenReturn(CONVERSION_RATE);
    }

    @Test
    public void testHandleDeposit_Success() {
        // create request
        DepositRequest request = new DepositRequest();
        request.setUsername(TEST_USERNAME);
        request.setDeposit(USD);
        request.setAmount(TRANSFER_AMOUNT);
        
        UserAccount updatedAccount = new UserAccount(TEST_USERNAME, USD, INITIAL_BALANCE + TRANSFER_AMOUNT);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(updatedAccount);
        
        // send request to service
        ResponseEntity<AccountResponse> response = accountService.handleDeposit(request);
        
        // check if deposit is done correctly
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.getStatus());
        assertEquals(TEST_USERNAME, body.getData().get("username"));
        assertEquals(USD, body.getData().get("currencyType"));
        assertEquals(INITIAL_BALANCE + TRANSFER_AMOUNT, body.getData().get("balance"));
        
        verify(userAccountRepository).save(any(UserAccount.class));
    }
    
    @Test
    public void testHandleDeposit_NewAccount() {
        // deposit to a new account request
        String newCurrency = "gbp";
        DepositRequest request = new DepositRequest();
        request.setUsername(TEST_USERNAME);
        request.setDeposit(newCurrency);
        request.setAmount(TRANSFER_AMOUNT);
        
        when(userAccountRepository.findByUsernameAndCurrencyCode(TEST_USERNAME, newCurrency))
            .thenReturn(Optional.empty());
        UserAccount newAccount = new UserAccount(TEST_USERNAME, newCurrency, TRANSFER_AMOUNT);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(newAccount);
        
        // send request to service
        ResponseEntity<AccountResponse> response = accountService.handleDeposit(request);
        
        // check if it works
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.getStatus());
        assertEquals(TRANSFER_AMOUNT, body.getData().get("balance"));
        
        verify(userAccountRepository).save(any(UserAccount.class));
    }
    
    @Test
    public void testHandleDeposit_UserNotFound() {
        // request had wrong username
        DepositRequest request = new DepositRequest();
        request.setUsername("nonexistent");
        request.setDeposit(USD);
        request.setAmount(TRANSFER_AMOUNT);
        
        // send request to service
        ResponseEntity<AccountResponse> response = accountService.handleDeposit(request);
        
        // this should be an error
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.getStatus());
        assertEquals("Username not found", body.getMessage());
        
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }
    
    @Test
    public void testHandleTransfer_Success() {
        // request to transfer amounts
        TransferRequest request = new TransferRequest();
        request.setUsername(TEST_USERNAME);
        request.setFromAccount(USD);
        request.setToAccount(EUR);
        request.setAmount(TRANSFER_AMOUNT);
        
        UserAccount updatedFromAccount = new UserAccount(TEST_USERNAME, USD, INITIAL_BALANCE - TRANSFER_AMOUNT);
        UserAccount updatedToAccount = new UserAccount(TEST_USERNAME, EUR, INITIAL_BALANCE + (TRANSFER_AMOUNT * CONVERSION_RATE));
        
        when(userAccountRepository.save(any(UserAccount.class)))
            .thenReturn(updatedFromAccount)
            .thenReturn(updatedToAccount);
        
        // send request to service
        ResponseEntity<AccountResponse> response = accountService.handleTransfer(request);
        
        // should successfully transfer
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.getStatus());
        assertEquals(TEST_USERNAME, body.getData().get("username"));
        assertEquals(USD, body.getData().get("fromAccount"));
        assertEquals(EUR, body.getData().get("toAccount"));
        assertEquals(TRANSFER_AMOUNT * CONVERSION_RATE, body.getData().get("amountTransferred"));
        assertEquals(INITIAL_BALANCE - TRANSFER_AMOUNT, body.getData().get("fromAccountBalance"));
        assertEquals(INITIAL_BALANCE + (TRANSFER_AMOUNT * CONVERSION_RATE), body.getData().get("toAccountBalance"));
        
        verify(userAccountRepository, times(2)).save(any(UserAccount.class));
    }
    
    @Test
    public void testHandleTransfer_InsufficientFunds() {
        // request for transfer where fromAccount doesn't have enough funds
        TransferRequest request = new TransferRequest();
        request.setUsername(TEST_USERNAME);
        request.setFromAccount(USD);
        request.setToAccount(EUR);
        request.setAmount(INITIAL_BALANCE + 1); 
        
        // send request to service
        ResponseEntity<AccountResponse> response = accountService.handleTransfer(request);
        
        // should be an error
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.getStatus());
        assertEquals("Insufficient funds or invalid fromAccount", body.getMessage());
        
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }
    
    @Test
    public void testHandleTransfer_CreateNewToAccount() {
        // create request where toAccount needs to created
        String newCurrency = "jpy";
        TransferRequest request = new TransferRequest();
        request.setUsername(TEST_USERNAME);
        request.setFromAccount(USD);
        request.setToAccount(newCurrency);
        request.setAmount(TRANSFER_AMOUNT);
        
        when(userAccountRepository.findByUsernameAndCurrencyCode(TEST_USERNAME, newCurrency))
            .thenReturn(Optional.empty());
        when(currencyService.calculateRate(USD, newCurrency)).thenReturn(110.0);
        
        UserAccount updatedFromAccount = new UserAccount(TEST_USERNAME, USD, INITIAL_BALANCE - TRANSFER_AMOUNT);
        UserAccount newToAccount = new UserAccount(TEST_USERNAME, newCurrency, TRANSFER_AMOUNT * 110.0);
        
        when(userAccountRepository.save(any(UserAccount.class)))
            .thenReturn(updatedFromAccount)
            .thenReturn(newToAccount);
        
        // send request to service
        ResponseEntity<AccountResponse> response = accountService.handleTransfer(request);
        
        // should successfully create new account and transfer
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.getStatus());
        assertEquals(newCurrency, body.getData().get("toAccount"));
        assertEquals(TRANSFER_AMOUNT * 110.0, body.getData().get("amountTransferred"));
    }
    
    @Test
    public void testGetUserAccounts_Success() {
        // create response to get accounts of a user
        ResponseEntity<AccountResponse> response = accountService.getUserAccounts(TEST_USERNAME);
        
        // check if it is a success
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Double> accounts = (Map<String, Double>) body.getData().get("accounts");
        assertNotNull(accounts);
        assertEquals(2, accounts.size());
        assertEquals(INITIAL_BALANCE, accounts.get(USD));
        assertEquals(INITIAL_BALANCE, accounts.get(EUR));
    }
    
    @Test
    public void testGetUserAccounts_UserNotFound() {
        // error response for user that doesn't exist
        ResponseEntity<AccountResponse> response = accountService.getUserAccounts("nonexistent");
        
        // should be an error
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.getStatus());
        assertEquals("Username not found", body.getMessage());
    }
    
    @Test
    public void testUpdateAccountBalance_Success() {
        // test is PUT works
        double newBalance = 1500.0;
        UserAccount updatedAccount = new UserAccount(TEST_USERNAME, USD, newBalance);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(updatedAccount);
        
        // get the response from service
        ResponseEntity<AccountResponse> response = accountService.updateAccountBalance(TEST_USERNAME, USD, newBalance);
        
        // should be a success
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.getStatus());
        assertEquals(TEST_USERNAME, body.getData().get("username"));
        assertEquals(USD, body.getData().get("currencyType"));
        assertEquals(newBalance, body.getData().get("newBalance"));
        
        verify(userAccountRepository).save(any(UserAccount.class));
    }
    
    @Test
    public void testUpdateAccountBalance_CreateNewAccount() {
        // creating new account with following params
        String newCurrency = "cad";
        double newBalance = 500.0;
        
        when(userAccountRepository.findByUsernameAndCurrencyCode(TEST_USERNAME, newCurrency))
            .thenReturn(Optional.empty());
        UserAccount newAccount = new UserAccount(TEST_USERNAME, newCurrency, newBalance);
        when(userAccountRepository.save(any(UserAccount.class))).thenReturn(newAccount);
        
        // get response from service
        ResponseEntity<AccountResponse> response = accountService.updateAccountBalance(TEST_USERNAME, newCurrency, newBalance);
        
        // this should be a successful creation of an account
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.getStatus());
        assertEquals(newBalance, body.getData().get("newBalance"));
        assertEquals(newCurrency, body.getData().get("currencyType"));
    }
    
    @Test
    public void testDeleteAccount_Success() {
        // delete an account and get the response
        ResponseEntity<AccountResponse> response = accountService.deleteAccount(TEST_USERNAME, USD);
        
        // should be a successful deletion
        assertEquals(HttpStatus.OK, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.getStatus());
        assertEquals(TEST_USERNAME, body.getData().get("username"));
        assertEquals(USD, body.getData().get("deletedAccount"));
        
        verify(userAccountRepository).delete(any(UserAccount.class));
    }
    
    @Test
    public void testDeleteAccount_AccountNotFound() {
        // try deleting fake account
        String nonExistentCurrency = "xyz";
        when(userAccountRepository.findByUsernameAndCurrencyCode(TEST_USERNAME, nonExistentCurrency))
            .thenReturn(Optional.empty());
        
        // get the response from service
        ResponseEntity<AccountResponse> response = accountService.deleteAccount(TEST_USERNAME, nonExistentCurrency);
        
        // shoudld be an error
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        AccountResponse body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.getStatus());
        assertEquals("Currency type account not found", body.getMessage());
        
        verify(userAccountRepository, never()).delete(any(UserAccount.class));
    }
}
