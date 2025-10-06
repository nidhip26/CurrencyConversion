package com.example.currencyapp.model;

import com.example.currencyapp.model.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<String>> getAllUsers() {
        logger.info("Fetching all users.");
        List<String> users = userService.getAllUsernames();
        logger.info("Returning users: {}", users);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> addUser(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        logger.info("Attempting to add user: {}", username);
        String message = userService.addUser(username);

        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("username", username);

        logger.info("User addition response: {}", response);
        return ResponseEntity.ok(response);
    }
}

