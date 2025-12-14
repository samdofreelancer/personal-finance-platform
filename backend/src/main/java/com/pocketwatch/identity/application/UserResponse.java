package com.pocketwatch.identity.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User Response DTO
 * 
 * Contains user information to return to frontend after authentication.
 * Does NOT contain sensitive information like passwords or Google tokens.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String email;
    private String name;
    private String picture;
}
