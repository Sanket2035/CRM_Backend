package com.crm.backend.controller;

import com.crm.backend.config.SecurityUtility;
import com.crm.backend.model.AuthResponse;
import com.crm.backend.model.LoginRequest;
import com.crm.backend.model.User;
import com.crm.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins="*")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody User user){
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            return ResponseEntity.badRequest().body(AuthResponse.builder()
                    .success(false)
                    .message("Username already taken!")
                    .build());
        }

        if(user.getRole() == null || user.getRole().isEmpty()){
            user.setRole("Sales");
        }

        user.setPassword(SecurityUtility.hashPassword(user.getPassword()));
        User savedUser = userRepository.save(user);
        String token = SecurityUtility.generateJwt(savedUser.getUsername(), savedUser.getRole());
        return ResponseEntity.ok().body(AuthResponse.builder()
                .success(true)
                .message("User registered successfully!")
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .token(token)
                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request){
        return userRepository.findByUsername(request.getUsername())
                .map(user -> {
                    if (SecurityUtility.checkPassword(request.getPassword(), user.getPassword())) {
                        String token = SecurityUtility.generateJwt(user.getUsername(), user.getRole());
                        return ResponseEntity.ok(AuthResponse.builder()
                                .success(true)
                                .message("Welcome back, " + user.getUsername() + "!")
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .token(token)
                                .build());
                    } else {
                        return ResponseEntity.badRequest().body(AuthResponse.builder()
                                .success(false)
                                .message("Invalid username or password.")
                                .build());
                    }
                })
                .orElseGet(() -> ResponseEntity.badRequest().body(AuthResponse.builder()
                        .success(false)
                        .message("Invalid username or password.")
                        .build()));
    }
}
