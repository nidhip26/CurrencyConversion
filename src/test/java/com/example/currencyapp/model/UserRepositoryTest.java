package com.example.currencyapp.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void shouldSaveAndFindUserSuccessfully() {
        // add new user
        User user = new User("testuser");

        // add it to repository
        userRepository.save(user);

        // check if it is added
        Optional<User> found = userRepository.findById("testuser");
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");

        // check if createdAt has something there too
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExist() {
        // user shouldn't exist
        Optional<User> found = userRepository.findById("ghostuser");

        // check that user is not there -> success
        assertThat(found).isNotPresent();
    }

    @Test
    void shouldDeleteUserSuccessfully() {
        // create new user and delete it
        User user = new User("deletethis");

        userRepository.save(user);
        userRepository.deleteById("deletethis");

        // check it should not be there -> success
        Optional<User> found = userRepository.findById("deletethis");

        
        assertThat(found).isNotPresent();
    }

    @Test
    void shouldThrowExceptionWhenSavingNullUsername() {
        // if empty string for username
        User user = new User();
        user.setUsername(null); 

        // error -> success
        assertThrows(Exception.class, () -> userRepository.saveAndFlush(user));
    }

    @Test
    void deletingNonExistentUserShouldNotThrow() {
        // fake user shouldn't cause error
        userRepository.deleteById("idontexist");


    }

    @Test
    void shouldSetCreatedAtWhenSavingUser() {
        // create new user
        User user = new User("createduser");

        userRepository.save(user);

        // check if there is a createdAt
        Optional<User> found = userRepository.findById("createduser");
        assertThat(found).isPresent();
        assertThat(found.get().getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }
}

