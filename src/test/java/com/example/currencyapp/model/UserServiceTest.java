package com.example.currencyapp.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class UserServiceTest {

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void getAllUsernames_shouldReturnListOfUsernames() {
        // add new users
        User user1 = new User("alice");
        User user2 = new User("bob");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user1, user2));

        // now get the users
        List<String> usernames = userService.getAllUsernames();

        // should match the two names
        assertEquals(2, usernames.size());
        assertTrue(usernames.contains("alice"));
        assertTrue(usernames.contains("bob"));
    }

    @Test
    void addUser_shouldReturnUserAlreadyExists_whenUserExists() {
        // add username and mock that it exists already
        String username = "existingUser";
        when(userRepository.existsById(username)).thenReturn(true);

        String result = userService.addUser(username);

        // should match this response -> success
        assertEquals("User already exists", result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_shouldRegisterUserSuccessfully_whenUserDoesNotExist() {
        // add new username
        String username = "newUser";
        when(userRepository.existsById(username)).thenReturn(false);
        String result = userService.addUser(username);

        // should return success message
        assertEquals("User registered successfully", result);
        verify(userRepository, times(1)).save(any(User.class));
    }
}
