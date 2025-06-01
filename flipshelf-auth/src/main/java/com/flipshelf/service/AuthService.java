package com.flipshelf.service;

import com.flipshelf.model.User;

import java.util.Optional;

public interface AuthService {
    void register(User user);
    User login(User user);
    void  upgradeToSeller(String email);

    Optional<User> getUserDetails(String email);

    boolean updateEmail(String token, String newEmail);
}
