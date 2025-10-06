package com.example.currencyapp.model;

import com.example.currencyapp.model.AccountResponse;
import com.example.currencyapp.model.DepositRequest;
import com.example.currencyapp.model.TransferRequest;
import com.example.currencyapp.model.UserAccount;
import com.example.currencyapp.model.UserAccountRepository;
import com.example.currencyapp.model.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final UserRepository userRepository;
    private final UserAccountRepository userAccountRepository;
    private final CurrencyService currencyService;

    @Autowired
    public AccountService(UserRepository userRepository, UserAccountRepository userAccountRepository, CurrencyService currencyService) {
        this.userRepository = userRepository;
        this.userAccountRepository = userAccountRepository;
        this.currencyService = currencyService;
    }

    public ResponseEntity<AccountResponse> handleDeposit(DepositRequest request) {
        String username = request.getUsername();
        double amount = request.getAmount();
        String currencyCode = request.getDeposit().toLowerCase();

        logger.info("Processing deposit: user={}, amount={}, currency={}", username, amount, currencyCode);

        if (!userRepository.existsById(username)) {
            logger.warn("Deposit failed: user {} not found", username);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AccountResponse("error", "Username not found"));
        }

        UserAccount account = userAccountRepository
                .findByUsernameAndCurrencyCode(username, currencyCode)
                .orElse(new UserAccount(username, currencyCode, 0.0));

        double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);
        userAccountRepository.save(account);

        logger.info("Deposit successful: user={}, newBalance={}", username, newBalance);

        AccountResponse response = new AccountResponse("success", "Deposit successful");
        response.addData("username", username);
        response.addData("currencyType", currencyCode);
        response.addData("balance", newBalance);

        return ResponseEntity.ok(response);
    }

    @Transactional
    public ResponseEntity<AccountResponse> handleTransfer(TransferRequest request) {
        String username = request.getUsername();
        double amount = request.getAmount();
        String fromAccount = request.getFromAccount().toLowerCase();
        String toAccount = request.getToAccount().toLowerCase();

        logger.info("Processing transfer: user={}, amount={}, from={}, to={}", username, amount, fromAccount, toAccount);

        if (!userRepository.existsById(username)) {
            logger.warn("Transfer failed: user {} not found", username);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AccountResponse("error", "Username not found"));
        }

        Optional<UserAccount> fromAccountOpt = userAccountRepository
                .findByUsernameAndCurrencyCode(username, fromAccount);

        if (fromAccountOpt.isEmpty() || fromAccountOpt.get().getBalance() < amount) {
            logger.warn("Transfer failed: insufficient funds or missing fromAccount for user {}", username);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AccountResponse("error", "Insufficient funds or invalid fromAccount"));
        }

        double rate = currencyService.calculateRate(fromAccount, toAccount);
        double convertedAmount = amount * rate;

        UserAccount fromAcct = fromAccountOpt.get();
        double newFromBalance = fromAcct.getBalance() - amount;
        fromAcct.setBalance(newFromBalance);
        userAccountRepository.save(fromAcct);

        UserAccount toAcct = userAccountRepository
                .findByUsernameAndCurrencyCode(username, toAccount)
                .orElse(new UserAccount(username, toAccount, 0.0));

        double newToBalance = toAcct.getBalance() + convertedAmount;
        toAcct.setBalance(newToBalance);
        userAccountRepository.save(toAcct);

        logger.info("Transfer successful: user={}, convertedAmount={}, fromBalance={}, toBalance={}",
                username, convertedAmount, newFromBalance, newToBalance);

        AccountResponse response = new AccountResponse("success", "Transfer successful");
        response.addData("username", username);
        response.addData("fromAccount", fromAccount);
        response.addData("toAccount", toAccount);
        response.addData("amountTransferred", convertedAmount);
        response.addData("fromAccountBalance", newFromBalance);
        response.addData("toAccountBalance", newToBalance);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<AccountResponse> getUserAccounts(String username) {
        logger.info("Fetching accounts for user {}", username);

        if (!userRepository.existsById(username)) {
            logger.warn("Get accounts failed: user {} not found", username);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AccountResponse("error", "Username not found"));
        }

        List<UserAccount> accounts = userAccountRepository.findByUsername(username);
        Map<String, Double> accountMap = new HashMap<>();

        for (UserAccount account : accounts) {
            accountMap.put(account.getCurrencyCode(), account.getBalance());
        }

        logger.info("Accounts retrieved for user {}", username);

        AccountResponse response = new AccountResponse("success", "Accounts retrieved");
        response.addData("accounts", accountMap);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<AccountResponse> updateAccountBalance(String username, String account, double amount) {
        String currencyCode = account.toLowerCase();

        logger.info("Updating account balance: user={}, account={}, newAmount={}", username, currencyCode, amount);

        if (!userRepository.existsById(username)) {
            logger.warn("Update failed: user {} not found", username);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AccountResponse("error", "Username not found"));
        }

        UserAccount userAccount = userAccountRepository
                .findByUsernameAndCurrencyCode(username, currencyCode)
                .orElse(new UserAccount(username, currencyCode, 0.0));

        userAccount.setBalance(amount);
        userAccountRepository.save(userAccount);

        logger.info("Account balance updated: user={}, currency={}, balance={}", username, currencyCode, amount);

        AccountResponse response = new AccountResponse("success", "Account balance updated successfully");
        response.addData("username", username);
        response.addData("currencyType", currencyCode);
        response.addData("newBalance", amount);

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<AccountResponse> deleteAccount(String username, String currencyCode) {
        currencyCode = currencyCode.toLowerCase();

        logger.info("Deleting account: user={}, currency={}", username, currencyCode);

        if (!userRepository.existsById(username)) {
            logger.warn("Delete failed: user {} not found", username);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AccountResponse("error", "Username not found"));
        }

        Optional<UserAccount> account = userAccountRepository
                .findByUsernameAndCurrencyCode(username, currencyCode);

        if (account.isEmpty()) {
            logger.warn("Delete failed: currency account {} not found for user {}", currencyCode, username);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new AccountResponse("error", "Currency type account not found"));
        }

        userAccountRepository.delete(account.get());

        logger.info("Account deleted: user={}, currency={}", username, currencyCode);

        AccountResponse response = new AccountResponse("success", "Currency type account deleted successfully");
        response.addData("username", username);
        response.addData("deletedAccount", currencyCode);

        return ResponseEntity.ok(response);
    }
}
