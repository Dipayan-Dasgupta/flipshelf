package com.flipshelf.serviceimpl;

import com.flipshelf.model.Role;
import com.flipshelf.model.User;
import com.flipshelf.repository.UserRepository;
import com.flipshelf.security.JwtUtil;
import com.flipshelf.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    @Transactional
    public void register(User user) {
        logger.info("Registering user with email: {}", user.getEmail());
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            logger.warn("Registration failed: Email already exists: {}", user.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(Role.ROLE_USER); // Always ROLE_USER
        userRepository.save(user);
        logger.info("User registered successfully with ROLE_USER: {}", user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public User login(User user) {
        logger.info("Login attempt for email: {}", user.getEmail());
        User storedUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed: User not found for email: {}", user.getEmail());
                    return new IllegalArgumentException("Invalid credentials");
                });
        if (passwordEncoder.matches(user.getPassword(), storedUser.getPassword())) {
            storedUser.setPassword(null); // Clear password
            logger.info("Login successful for user: {}", storedUser.getUserName());
            return storedUser;
        }
        logger.warn("Login failed: Invalid password for email: {}", user.getEmail());
        throw new IllegalArgumentException("Invalid credentials");
    }

    @Override
    public void upgradeToSeller(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        user.setRole(Role.ROLE_SELLER);
        userRepository.save(user);
        logger.info("User promoted to Seller");
    }
    @Override
    public Optional<User> getUserDetails(String email){
        return userRepository.findByEmail(email);
    }
    @Override
    @Transactional
    public boolean updateEmail(String token, String newEmail) {
        try {
            String jwt = token.substring(7);
            String currentEmail = jwtUtil.getEmailFromToken(jwt);
            Optional<User> optionalUser = userRepository.findByEmail(currentEmail);
            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                user.setEmail(newEmail);
                userRepository.save(user);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace(); // Log properly in prod
        }
        return false;
    }
}
