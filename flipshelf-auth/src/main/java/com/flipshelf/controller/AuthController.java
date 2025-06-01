package com.flipshelf.controller;

import com.flipshelf.model.User;
import com.flipshelf.repository.UserRepository;
import com.flipshelf.security.JwtUtil;
import com.flipshelf.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    private AuthService authService;
    @Autowired
    private JwtUtil jwtUtil;

    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody User user) {
        logger.info("Received registration request for email: {}", user.getEmail());
        authService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user) {
        logger.info("Received login request for email: {}", user.getEmail());
        User loggedInUser = authService.login(user);
        String token = jwtUtil.generateToken(loggedInUser.getUserName(), loggedInUser.getEmail(), loggedInUser.getRole());
        Map<String, Object> response = new HashMap<>();
        response.put("user", loggedInUser);
        response.put("token", token);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/upgrade-to-seller")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> upgradeToSeller(@RequestHeader("Authorization") String token, @RequestParam String email) {
        authService.upgradeToSeller(email);
        return ResponseEntity.status(HttpStatus.OK).body("User promoted to Seller");
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String token) {
        String jwt = token.substring(7);
        String emailFromToken = jwtUtil.getEmailFromToken(jwt);
        String role = jwtUtil.getRoleFromToken(jwt);
        Optional<User> user = authService.getUserDetails(emailFromToken);
        if (user.isEmpty()) {
            emailFromToken = null;
        }
        logger.info("email");
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", emailFromToken);
        claims.put("role", role);
        return ResponseEntity.ok(claims);
    }

    @PutMapping("/update-email")
    public ResponseEntity<String> updateEmail(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> request) {
        String newEmail = request.get("newEmail");
        if (newEmail == null || newEmail.isEmpty()) {
            return ResponseEntity.badRequest().body("New email cannot be empty");
        }

        boolean updated = authService.updateEmail(token, newEmail);
        if (updated) {
            return ResponseEntity.ok("Email updated successfully. Please log in again.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token or user not found");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        logger.error("Operation failed: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
