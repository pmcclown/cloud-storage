package ru.netology.cloudstorage.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.netology.cloudstorage.dto.JwtRequest;
import ru.netology.cloudstorage.dto.JwtResponse;
import ru.netology.cloudstorage.service.AuthService;

@RestController
@RequestMapping("/cloud")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "http://localhost**", allowCredentials = "true")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody JwtRequest authRequest) {
        String token = authService.login(authRequest);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("auth-token") String token) {
        authService.logout(token);
    }
}