package com.example.paytrack.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @GetMapping("/login")
    public Map<String, String> login(Authentication authentication) {
        return Map.of(
                "message", "Login successful",
                "email", authentication.getName()
        );
    }
}