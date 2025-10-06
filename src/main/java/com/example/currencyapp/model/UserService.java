package com.example.currencyapp.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    public List<String> getAllUsernames() {
        logger.info("Fetching all usernames from the database");
        List<String> usernames = userRepository.findAll().stream()
                .map(User::getUsername)
                .collect(Collectors.toList());
        logger.debug("Found {} usernames", usernames.size());
        return usernames;
    }

    public String addUser(String username) {
        logger.info("Attempting to add user: {}", username);
        if (userRepository.existsById(username)) {
            logger.warn("User {} already exists in the database", username);
            return "User already exists";
        }

        User user = new User(username);
        userRepository.save(user);
        logger.info("Added new user: {}", username);
        
        return "User registered successfully";
    }
}



