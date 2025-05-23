package de.zeroco.employeemanagement.model.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "app_user") // "user" is often a reserved keyword in SQL
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a user of the application, for authentication and authorization purposes.")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Unique identifier of the user.", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(unique = true, nullable = false)
    @Schema(description = "Username of the user, must be unique.", example = "newuser", required = true)
    private String username;

    @NotBlank
    @Size(min = 8, max = 100) // Store encoded password
    @Column(nullable = false)
    @Schema(description = "Password for the user. Will be encoded in the database. Minimum 8 characters.", example = "securePassword123", required = true, accessMode = Schema.AccessMode.WRITE_ONLY)
    private String password;

    @NotBlank
    @Column(nullable = false)
    @Schema(description = "Comma-separated list of roles for the user (e.g., 'ROLE_USER,ROLE_ADMIN'). Defaults to 'ROLE_USER' if not provided during registration.", example = "ROLE_USER")
    private String roles; // Comma-separated roles, e.g., "ROLE_USER,ROLE_ADMIN"
}
