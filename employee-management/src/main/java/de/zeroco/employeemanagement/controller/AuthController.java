package de.zeroco.employeemanagement.controller;

import de.zeroco.employeemanagement.model.security.User;
import de.zeroco.employeemanagement.payload.AuthRequest;
import de.zeroco.employeemanagement.payload.AuthResponse;
import de.zeroco.employeemanagement.repository.security.UserRepository;
import de.zeroco.employeemanagement.security.JwtUtil;
import de.zeroco.employeemanagement.security.UserDetailsServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication Controller", description = "Handles user authentication and registration.")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired
    private UserRepository userRepository; // For registration

    @Autowired
    private PasswordEncoder passwordEncoder; // For registration

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "Authenticate user",
               description = "Authenticates a user based on username and password, and returns a JWT token if successful.",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                   description = "Credentials for user authentication.",
                   required = true,
                   content = @Content(schema = @Schema(implementation = AuthRequest.class))
               ),
               responses = {
                   @ApiResponse(responseCode = "200", description = "Authentication successful, JWT token returned.",
                                content = @Content(schema = @Schema(implementation = AuthResponse.class))),
                   @ApiResponse(responseCode = "401", description = "Invalid credentials.",
                                content = @Content(mediaType = "application/json",
                                                   examples = @ExampleObject(value = "{ \"timestamp\": \"2023-03-15T10:30:00Z\", \"status\": 401, \"error\": \"Unauthorized\", \"message\": \"Incorrect username or password\", \"path\": \"/api/v1/auth/login\" }")))
               })
    public ResponseEntity<?> createAuthenticationToken(@Valid @RequestBody AuthRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.username, authenticationRequest.password)
            );
        } catch (BadCredentialsException e) {
            // It's better to handle this via GlobalExceptionHandler if we want consistent 401 JSON response
            // For now, re-throwing to be caught by a general handler or Spring Security's default.
            // Or, return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Incorrect username or password"));
            throw new BadCredentialsException("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(authenticationRequest.username);
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register")
    @Operation(summary = "Register new user",
               description = "Registers a new user with the provided details. Default role is 'ROLE_USER' if not specified.",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                   description = "User details for registration. Username and password are required. Roles are optional (defaults to ROLE_USER).",
                   required = true,
                   content = @Content(schema = @Schema(implementation = User.class))
               ),
               responses = {
                   @ApiResponse(responseCode = "200", description = "User registered successfully.",
                                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"User registered successfully!\" }"))),
                   @ApiResponse(responseCode = "400", description = "Bad Request (e.g., username already taken, validation errors).",
                                content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{ \"message\": \"Error: Username is already taken!\" }")))
               })
    public ResponseEntity<?> registerUser(@Valid @RequestBody User newUser) {
        if (userRepository.findByUsername(newUser.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Username is already taken!"));
        }
        // Encode password
        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        // Simple default role, you might want to make this more flexible
        if (newUser.getRoles() == null || newUser.getRoles().isEmpty()) {
            newUser.setRoles("ROLE_USER");
        }
        userRepository.save(newUser);
        return ResponseEntity.ok(Map.of("message", "User registered successfully!"));
    }
}
