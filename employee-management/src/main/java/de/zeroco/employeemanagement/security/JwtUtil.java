package de.zeroco.employeemanagement.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.Base64;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretString;

    @Value("${jwt.expirationMs}")
    private long expirationMs;

    private SecretKey secretKey;

    // Initialize the key once it's read from properties
    @PostConstruct
    private void init() {
        // Ensure the secret key is long enough for HS256 (256 bits / 32 bytes)
        // If your secretString from properties is shorter, you might need a different approach
        // or ensure it's a Base64 encoded key that meets the length requirement.
        // For simplicity, if it's not Base64, we'll use it directly, assuming it's strong enough.
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secretString);
        } catch (IllegalArgumentException e) {
            // If not Base64, use the string bytes directly.
            // This is less secure if the string is simple.
            // Consider using a utility to generate a secure key if this is the case.
            keyBytes = secretString.getBytes();
        }
        // Ensure the key is at least 32 bytes for HS256
        if (keyBytes.length < 32) {
            // Pad or handle this error appropriately. For now, let's throw an exception or log.
            // This is a simplified handling. Production systems need robust key management.
            // One option: byte[] paddedKeyBytes = new byte[32]; System.arraycopy(keyBytes, 0, paddedKeyBytes, 0, keyBytes.length); keyBytes = paddedKeyBytes;
            throw new IllegalArgumentException("JWT secret key is too short. Must be at least 256 bits (32 bytes) for HS256, preferably Base64 encoded.");
        }
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // You can add more claims here, like roles
        claims.put("roles", userDetails.getAuthorities());
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
