package com.example.currencyapp.model;

import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import com.example.currencyapp.model.AccountResponse;
import com.example.currencyapp.model.DepositRequest;
import com.example.currencyapp.model.TransferRequest;
import com.example.currencyapp.model.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private static final Logger logger = LoggerFactory.getLogger(AccountController.class);

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> handleAccountOperation(@RequestBody Map<String, Object> request) {
        logger.info("Received account operation request: {}", request);

        if (request.containsKey("deposit")) {
            DepositRequest depositRequest = new DepositRequest(
                    (String) request.get("username"),
                    ((Number) request.get("amount")).doubleValue(),
                    (String) request.get("deposit")
            );
            logger.info("Processing deposit for username: {}", depositRequest.getUsername());
            return accountService.handleDeposit(depositRequest);
        } else if (request.containsKey("fromAccount") && request.containsKey("toAccount")) {
            TransferRequest transferRequest = new TransferRequest(
                    (String) request.get("username"),
                    ((Number) request.get("amount")).doubleValue(),
                    (String) request.get("fromAccount"),
                    (String) request.get("toAccount")
            );
            logger.info("Processing transfer from account: {} to account: {}", transferRequest.getFromAccount(), transferRequest.getToAccount());
            return accountService.handleTransfer(transferRequest);
        } else {
            logger.error("Invalid request body: {}", request);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AccountResponse("error", "Invalid request body"));
        }
    }

    @GetMapping
    public ResponseEntity<AccountResponse> getAccounts(@RequestBody Map<String, String> request) {
        logger.info("Received request to get accounts for username: {}", request.get("username"));
        return accountService.getUserAccounts(request.get("username"));
    }

    @PutMapping
    public ResponseEntity<AccountResponse> updateAccount(@RequestBody Map<String, Object> request) {
        String username = (String) request.get("username");
        String account = (String) request.get("account");
        double amount = ((Number) request.get("amount")).doubleValue();
        
        logger.info("Received request to update account: {} for username: {} with amount: {}", account, username, amount);
        return accountService.updateAccountBalance(username, account, amount);
    }

    @DeleteMapping
    public ResponseEntity<AccountResponse> deleteAccount(@RequestBody Map<String, String> request) {
        logger.info("Received request to delete account: {} for username: {}", request.get("delete"), request.get("username"));
        return accountService.deleteAccount(request.get("username"), request.get("delete"));
    }
}
