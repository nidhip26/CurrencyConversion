package com.example.currencyapp.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAllUsers() throws Exception {
        //mocks 3 users 
        when(userService.getAllUsernames()).thenReturn(Arrays.asList("alice", "bob", "carol"));

        //should return 3 users -> success
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value("alice"))
                .andExpect(jsonPath("$[1]").value("bob"))
                .andExpect(jsonPath("$[2]").value("carol"));
    }

    @Test
    void shouldAddUserSuccessfully() throws Exception {
        //add a user
        String username = "dave";
        when(userService.addUser(username)).thenReturn("User added successfully");

        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("username", username);

        //response should be user is added -> success
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestPayload)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("User added successfully"))
                .andExpect(jsonPath("$.username").value(username));
    }



    @Test
    void shouldReturnBadRequestWhenUsernameMissing() throws Exception {
        // if no username
        Map<String, String> invalidPayload = new HashMap<>();

        // successfully returns nothing
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.username").doesNotExist());
    }

    @Test
    void shouldHandleEmptyUsernameGracefully() throws Exception {
        //if no username in param
        Map<String, String> invalidPayload = new HashMap<>();
        invalidPayload.put("username", "");

        //can still add it
        when(userService.addUser("")).thenReturn("User added successfully"); 

        //should be a success
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(""));
    }


}


