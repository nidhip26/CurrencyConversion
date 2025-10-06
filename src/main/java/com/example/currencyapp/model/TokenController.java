package com.example.currencyapp.model;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@RestController
public class TokenController {

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    @GetMapping("/token")
    public Map<String, String> getToken(
        @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient authorizedClient,
        @AuthenticationPrincipal OAuth2User principal
    ) {
        logger.info("Request received to get the access token.");

        if (authorizedClient == null || authorizedClient.getAccessToken() == null) {
            logger.error("No authorized client or access token found for the user: {}", principal.getName());
            return Map.of("error", "No valid access token available.");
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        logger.info("Returning access token for user: {}", principal.getName());

        return Map.of(
            "accessToken", accessToken
        );
    }
}

