package com.stage.digibackend.security.services;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


public class TokenService {
    private final SessionRegistry sessionRegistry;

    public TokenService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    public int countValidTokensForUser(String username) {
        int count = 0;

        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof Authentication) {
                Authentication auth = (Authentication) principal;
                if (auth.getName().equals(username)) {
                    for (SessionInformation sessionInfo : sessionRegistry.getAllSessions(principal, false)) {
                        if (!sessionInfo.isExpired()) {
                            String token = extractTokenFromAuthentication(auth);
                            if (token != null) {
                                count++;
                            }
                        }
                    }
                }
            }
        }

        return count;
    }

    private String extractTokenFromAuthentication(Authentication authentication) {
        if (authentication instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken authToken = (UsernamePasswordAuthenticationToken) authentication;
            // Assuming the token is stored as credentials in the authentication object
            return (String) authToken.getCredentials();
        }
        return null;
    }
}



