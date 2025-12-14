package com.pocketwatch.identity.infrastructure;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import java.util.Collections;

/**
 * JWT Authentication Token
 * 
 * Custom authentication token that holds:
 * - JWT as principal
 * - User ID as credentials
 * 
 * Used by Spring Security to maintain authentication in SecurityContext.
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    
    private final String jwt;
    private final String userId;
    
    public JwtAuthenticationToken(String jwt, String userId) {
        super(Collections.emptyList());
        this.jwt = jwt;
        this.userId = userId;
        this.setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return jwt;
    }
    
    @Override
    public Object getPrincipal() {
        return jwt;
    }
    
    public String getUserId() {
        return userId;
    }
}
