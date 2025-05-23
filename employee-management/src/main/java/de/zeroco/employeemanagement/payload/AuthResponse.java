package de.zeroco.employeemanagement.payload;

import io.swagger.v3.oas.annotations.media.Schema;

public class AuthResponse {
    @Schema(description = "JWT token for authentication.", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbnVzZXIiLCJyb2xlcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImlhdCI6MTY3ODg4NjQwMCwiZXhwIjoxNjc4ODkwMDAwfQ.exampleToken")
    public String token;

    public AuthResponse(String token) {
        this.token = token;
    }
}
