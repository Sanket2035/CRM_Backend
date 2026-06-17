package com.crm.backend.config;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Date;

public class SecurityUtility {

    private static final String SECRET_KEY = "ApexCRM_Enterprise_Secure_Secret_Key_Signature";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    private static final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ----------------------------------------------------
    // PASSWORD HASHING (using standard BCrypt)
    // ----------------------------------------------------
    public static String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    public static boolean checkPassword(String password, String hashed) {
        try {
            return passwordEncoder.matches(password, hashed);
        } catch (Exception e) {
            return false;
        }
    }

    // ----------------------------------------------------
    // STATELESS JWT UTILITY (using Auth0 java-jwt)
    // ----------------------------------------------------
    public static String generateJwt(String username, String role) {
        return JWT.create()
                .withSubject(username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + 86400000)) // 1 day expiration
                .sign(ALGORITHM);
    }

    public static Claims parseJwt(String token) {
        if (token == null) {
            return null;
        }
        try {
            JWTVerifier verifier = JWT.require(ALGORITHM).build();
            DecodedJWT decodedJWT = verifier.verify(token);
            String username = decodedJWT.getSubject();
            String role = decodedJWT.getClaim("role").asString();
            if (username == null || role == null) {
                return null;
            }
            return new Claims(username, role);
        } catch (Exception e) {
            return null;
        }
    }

    public static class Claims {
        private final String username;
        private final String role;

        public Claims(String username, String role) {
            this.username = username;
            this.role = role;
        }

        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
}
