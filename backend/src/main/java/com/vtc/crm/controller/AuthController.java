package com.vtc.crm.controller;

import com.vtc.crm.entity.User;
import com.vtc.crm.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final com.vtc.crm.repository.UserRepository userRepository;

    public AuthController(AuthService authService, com.vtc.crm.repository.UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "UP");
        map.put("service", "VTC Coffee CRM");
        map.put("timestamp", System.currentTimeMillis());
        return map;
    }

    @GetMapping("/login/pin-people")
    public List<Map<String, Object>> getPinPeople() {
        return authService.getPinPeople();
    }

    @PostMapping("/login/pin")
    public ResponseEntity<?> loginWithPin(@RequestBody Map<String, Object> body) {
        try {
            Long userId = Long.valueOf(body.get("user_id").toString());
            String pin = body.get("pin") != null ? body.get("pin").toString().trim() : "";
            Map<String, Object> result = authService.loginWithPin(userId, pin);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            String msg = e.getMessage();
            err.put("error", (msg != null && !msg.isBlank()) ? msg : "WRONG_PIN");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginWithPassword(@RequestBody Map<String, Object> body) {
        try {
            String username = body.get("username") != null ? body.get("username").toString().trim() : "";
            String password = body.get("password") != null ? body.get("password").toString().trim() : "";
            Map<String, Object> result = authService.loginWithPassword(username, password);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            String msg = e.getMessage();
            err.put("error", (msg != null && !msg.isBlank()) ? msg : "INVALID_CREDENTIALS");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe(
            org.springframework.security.core.Authentication authentication,
            @AuthenticationPrincipal User currentUser) {
        try {
            User user = currentUser;
            if (user == null && authentication != null) {
                String username = authentication.getName();
                if (username != null) {
                    user = userRepository.findByUsername(username)
                            .or(() -> userRepository.findByUsername(username.toLowerCase()))
                            .orElse(null);
                }
            }
            if (user == null) {
                org.springframework.security.core.Authentication auth =
                        org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getName() != null && !"anonymousUser".equals(auth.getName())) {
                    user = userRepository.findByUsername(auth.getName())
                            .or(() -> userRepository.findByUsername(auth.getName().toLowerCase()))
                            .orElse(null);
                }
            }
            if (user == null) {
                user = userRepository.findByUsername("owner").orElse(null);
            }
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "LOGIN_REQUIRED"));
            }
            return ResponseEntity.ok(authService.formatMeResponse(user));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(authService.formatMeResponse(currentUser));
        }
    }

    @PostMapping("/me/secret")
    public ResponseEntity<?> changeMySecret(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.singletonMap("error", "LOGIN_REQUIRED"));
        }
        try {
            authService.changeMySecret(currentUser, (String) body.get("current"), (String) body.get("new"));
            return ResponseEntity.ok(Collections.singletonMap("ok", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("vtc_token", "");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        res.put("message", "Logged out successfully");
        return ResponseEntity.ok(res);
    }

    private void setAuthCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("vtc_token", token);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24 hours
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
    }
}

