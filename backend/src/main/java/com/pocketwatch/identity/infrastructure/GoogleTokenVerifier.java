package com.pocketwatch.identity.infrastructure;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
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
 * Uses Google's JWKS (JSON Web Key Set) endpoint to get public keys
 * for signature verification.
 * 
 * CRITICAL SECURITY:
 * - ALWAYS verify token signature before trusting claims
 * - Verify that aud (audience) matches our client ID
 * - Cache public keys to avoid excessive network requests
 */
@Component
@Slf4j
public class GoogleTokenVerifier {
    
    @Value("${google.client.id}")
    private String googleClientId;
    
    private static final String GOOGLE_JWKS_URL = 
        "https://www.googleapis.com/oauth2/v1/certs";
    
    private Map<String, String> publicKeysCache = new HashMap<>();
    private long cacheExpiryTime = 0;
    
    /**
     * Verify Google ID Token and extract claims
     * 
     * @param idToken ID Token from Google (JWT format)
     * @return Decoded token claims
     * @throws Exception if token is invalid
     */
    public Claims verifyGoogleToken(String idToken) throws Exception {
        
        try {
            log.debug("Verifying Google ID Token...");
            
            // Parse JWT header to get the key ID (kid)
            String[] parts = idToken.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid JWT format");
            }
            
            // Decode header
            String headerJson = new String(
                Base64.getUrlDecoder().decode(parts[0]), 
                StandardCharsets.UTF_8
            );
            JsonObject header = new Gson().fromJson(headerJson, JsonObject.class);
            String keyId = header.get("kid").getAsString();
            
            log.debug("Token key ID: {}", keyId);
            
            // Get the public key for this key ID
            String publicKeyPem = getPublicKey(keyId);
            
            // Convert PEM to PublicKey
            PublicKey publicKey = convertPemToPublicKey(publicKeyPem);
            
            // Verify and parse the JWT
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
            // Google returns audience as a Set<String>
            boolean audienceMatches = false;
            if (audienceObj instanceof String) {
                audienceMatches = googleClientId.equals(audienceObj);
            } else if (audienceObj instanceof java.util.Set) {
                // Audience is a Set, check if our client ID is in the set
                @SuppressWarnings("unchecked")
                java.util.Set<String> audiences = (java.util.Set<String>) audienceObj;
                audienceMatches = audiences.contains(googleClientId);
            }
            
            if (!audienceMatches) {
                throw new IllegalArgumentException(
                    String.format("Token audience '%s' does not match expected client ID '%s'", 
                        audienceObj, googleClientId));
            }
            
            log.info("Successfully verified Google ID Token for user: {}", 
                claims.get("email"));
            
            return claims;
            
        } catch (SignatureException e) {
            log.error("Invalid token signature: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid token signature", e);
        } catch (Exception e) {
            log.error("Token verification failed: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Token verification failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get public key from Google's JWKS endpoint
     * 
     * @param keyId Key ID from JWT header
     * @return Public key in PEM format
     * @throws Exception if key cannot be retrieved
     */
    private String getPublicKey(String keyId) throws Exception {
        
        // Check cache (valid for 1 hour)
        if (publicKeysCache.containsKey(keyId) && System.currentTimeMillis() < cacheExpiryTime) {
            log.debug("Using cached public key for kid: {}", keyId);
            return publicKeysCache.get(keyId);
        }
        
        // Fetch Google's public keys
        log.debug("Fetching public keys from Google JWKS endpoint...");
        String response = fetchFromUrl(GOOGLE_JWKS_URL);
        
        JsonObject certs = new Gson().fromJson(response, JsonObject.class);
        String publicKeyPem = certs.get(keyId).getAsString();
        
        // Cache the keys (1 hour)
        cacheExpiryTime = System.currentTimeMillis() + (3600 * 1000);
        publicKeysCache.put(keyId, publicKeyPem);
        
        log.debug("Successfully fetched and cached public key for kid: {}", keyId);
        return publicKeyPem;
    }
    
    /**
     * Fetch content from a URL
     * 
     * @param urlString URL to fetch from
     * @return Response body as string
     * @throws Exception if fetch fails
     */
    private String fetchFromUrl(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        } finally {
            connection.disconnect();
        }
    }
    
    /**
     * Convert PEM format public key string to PublicKey object
     * 
     * Google returns certificates as base64-encoded X.509 DER format.
     * We need to properly format it as PEM with line breaks every 64 characters,
     * then use CertificateFactory to parse it.
     * 
     * @param certificateBase64 Certificate in base64 format (may have PEM headers)
     * @return PublicKey object
     * @throws Exception if conversion fails
     */
    private PublicKey convertPemToPublicKey(String certificateBase64) throws Exception {
        // First, remove any existing PEM headers/footers and whitespace
        String cleanCert = certificateBase64
            .replaceAll("-----BEGIN[^-]*-----", "")
            .replaceAll("-----END[^-]*-----", "")
            .replaceAll("\\s+", "");
        
        // Now add proper PEM formatting with line breaks
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN CERTIFICATE-----\n");
        
        // Add line breaks every 64 characters (standard PEM format)
        int index = 0;
        while (index < cleanCert.length()) {
            int end = Math.min(index + 64, cleanCert.length());
            pem.append(cleanCert, index, end);
            pem.append("\n");
            index = end;
        }
        
        pem.append("-----END CERTIFICATE-----");
        
        log.debug("Parsing X.509 certificate from PEM format");
        
        // Parse the PEM certificate using CertificateFactory
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        java.io.InputStream certStream = new java.io.ByteArrayInputStream(
            pem.toString().getBytes(StandardCharsets.UTF_8)
        );
        
        X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(certStream);
        return certificate.getPublicKey();
    }
}
