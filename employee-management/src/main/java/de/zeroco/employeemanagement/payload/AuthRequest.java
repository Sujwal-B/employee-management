package de.zeroco.employeemanagement.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    @Schema(description = "Username of the user.", example = "adminuser", required = true)
    @NotBlank
    public String username;

    @Schema(description = "Password of the user.", example = "password123", required = true)
    @NotBlank
    public String password;
}
