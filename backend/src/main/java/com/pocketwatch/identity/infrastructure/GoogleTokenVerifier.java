package com.pocketwatch.identity.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Google OIDC Token Verifier
 * 
 * Responsible for:
 * - Verifying Google ID Token signature
 * - Extracting claims from verified token
 * - Validating token expiration and audience
 * 
 * CRITICAL SECURITY:
 * - ALWAYS verify token signature before trusting claims
 * - Verify that aud (audience) matches our client ID
 * - Use Google's public keys for signature verification
 */
@Component
@Slf4j
public class GoogleTokenVerifier {
    
    @Value("${google.client.id}")
    private String googleClientId;
    
    private static final String GOOGLE_OAUTH2_CERTS_URL = 
        "https://www.googleapis.com/oauth2/v1/certs";
    private Map<String, PublicKey> publicKeysCache = new HashMap<>();
    
    /**
     * Verify Google ID Token and extract claims
     * 
     * @param idToken ID Token from Google (JWT format)
     * @return Decoded token claims
     * @throws Exception if token is invalid
     */
    public Claims verifyGoogleToken(String idToken) throws Exception {
        
        try {
            // Parse the header to get the key ID (kid)
            String[] parts = idToken.split("\\.");
            String headerJson = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
            JsonObject header = new Gson().fromJson(headerJson, JsonObject.class);
            String keyId = header.get("kid").getAsString();
            
            // Get the public key for this key ID
            PublicKey publicKey = getPublicKey(keyId);
            
            // Verify the token signature
            Claims claims = Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(idToken)
                .getPayload();
            
            // Verify audience matches our client ID
            Object audienceObj = claims.getAudience();
            if (audienceObj == null) {
                throw new IllegalArgumentException("Token missing 'aud' (audience) claim");
            }
            
            // Handle audience as either String or Set<String>
            boolean audienceMatches = false;
            if (audienceObj instanceof String) {
                audienceMatches = googleClientId.equals(audienceObj);
            }
            
            if (!audienceMatches) {
                throw new IllegalArgumentException(
                    String.format("Token audience '%s' does not match expected client ID '%s'", 
                        audienceObj, googleClientId));
            }
            
            // Verify token hasn't expired (JJWT does this automatically)
            log.debug("Successfully verified Google ID Token for user: {}", 
                claims.get("email"));
            
            return claims;
            
        } catch (SignatureException e) {
            log.error("Invalid token signature: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token signature", e);
        } catch (Exception e) {
            log.error("Token verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("Token verification failed", e);
        }
    }
    
    /**
     * Get public key from Google's JWKS endpoint
     * 
     * @param keyId Key ID from JWT header
     * @return Public key
     * @throws Exception if key cannot be retrieved
     */
    private PublicKey getPublicKey(String keyId) throws Exception {
        
        // Check cache first
        if (publicKeysCache.containsKey(keyId)) {
            return publicKeysCache.get(keyId);
        }
        
        // Fetch Google's public keys
        String response = new Scanner(new URL(GOOGLE_OAUTH2_CERTS_URL).openStream())
            .useDelimiter("\\A").next();
        
        JsonObject certs = new Gson().fromJson(response, JsonObject.class);
        String certificate = certs.get(keyId).getAsString();
        
        // Convert certificate string to PublicKey
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(
            new java.io.ByteArrayInputStream(
                ("-----BEGIN CERTIFICATE-----\n" + 
                 certificate + 
                 "\n-----END CERTIFICATE-----")
                .getBytes()
            )
        );
        
        PublicKey publicKey = cert.getPublicKey();
        publicKeysCache.put(keyId, publicKey);
        
        return publicKey;
    }
}
