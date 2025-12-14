package com.pocketwatch.identity.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Google Login Request DTO
 * 
 * Accepts Google ID Token from frontend.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleLoginRequest {
    private String idToken;
}
