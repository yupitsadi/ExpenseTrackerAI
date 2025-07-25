package com.expense.ExpenseTracker.controller;

import com.expense.ExpenseTracker.dto.AuthRequest;
import com.expense.ExpenseTracker.dto.AuthResponse;
import com.expense.ExpenseTracker.messaging.MessageProducer;
import com.expense.ExpenseTracker.model.User;
import com.expense.ExpenseTracker.repository.UserRepository;
import com.expense.ExpenseTracker.service.JwtService;
import com.expense.ExpenseTracker.service.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final MessageProducer messageProducer;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> registerUser(@RequestBody AuthRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().build();
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        // Generate token for the new user
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        String token = jwtService.generateToken(userDetails);
        String userId = userDetails.getUser().getId();
        
        // Send signup notification and analytics event
        try {
            // Send welcome notification
            messageProducer.sendNotification("Welcome to Expense Tracker! Your account has been created successfully.");
            
            // Track signup in analytics
            String analyticsData = String.format("{\"userId\":\"%s\",\"email\":\"%s\",\"timestamp\":\"%s\"}",
                    userId, user.getEmail(), new Date().toString());
            messageProducer.sendAnalytics("user_signup", analyticsData);
            
            log.info("Sent signup notifications for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send signup notifications for user: {}", user.getEmail(), e);
            // Don't fail the request if notifications fail
        }
        
        return ResponseEntity.ok(new AuthResponse(token, userId));
    }

    @PostMapping("/forgot-password")
    public void initiatePasswordReset(@RequestParam String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            String token = UUID.randomUUID().toString();
            user.setResetToken(token);
            user.setResetTokenExpiry(new Date(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 minutes
            userRepository.save(user);

            // Send password reset email asynchronously
            try {
                String resetLink = "http://localhost:3000/reset-password?token=" + token;
                String subject = "Password Reset Request";
                String content = String.format(
                    "Hello,\n\n" +
                    "You have requested to reset your password. Click the link below to proceed:\n" +
                    "%s\n\n" +
                    "If you didn't request this, please ignore this email.\n" +
                    "This link will expire in 15 minutes.",
                    resetLink
                );
                
                messageProducer.sendEmail(email, subject, content);
                log.info("Password reset email sent to: {}", email);
                
                // Track password reset request in analytics
                String analyticsData = String.format(
                    "{\"userId\":\"%s\",\"email\":\"%s\",\"timestamp\":\"%s\"}",
                    user.getId(), email, new Date().toString()
                );
                messageProducer.sendAnalytics("password_reset_requested", analyticsData);
                
            } catch (Exception e) {
                log.error("Failed to send password reset email to: {}", email, e);
                throw new RuntimeException("Failed to send password reset email", e);
            }
        } else {
            log.warn("Password reset requested for non-existent email: {}", email);
            // Don't reveal that the email doesn't exist for security reasons
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        Optional<User> optionalUser = userRepository.findByResetToken(token);
        if (optionalUser.isEmpty() || optionalUser.get().getResetTokenExpiry().before(new Date())) {
            return ResponseEntity.badRequest().body("Invalid or expired reset token");
        }
        
        User user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);
        
        // Send password changed notification
        try {
            messageProducer.sendEmail(
                user.getEmail(),
                "Password Changed Successfully",
                "Your password has been changed successfully.\n\n" +
                "If you didn't make this change, please contact our support team immediately."
            );
            log.info("Password reset successful for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password change confirmation to: {}", user.getEmail(), e);
            // Don't fail the request if notification fails
        }
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody AuthRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtService.generateToken(userDetails);
        String userId = userDetails.getUser().getId();

        // Set JWT as HTTP-only cookie named 'token'
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true)
                .secure(false) // Set to true in production (HTTPS)
                .path("/")
                .maxAge(24 * 60 * 60) // 1 day
                .sameSite("Lax")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        // Track login in analytics
        try {
            String analyticsData = String.format(
                "{\"userId\":\"%s\",\"email\":\"%s\",\"timestamp\":\"%s\"}",
                userId, request.getEmail(), new Date().toString()
            );
            messageProducer.sendAnalytics("user_login", analyticsData);
        } catch (Exception e) {
            log.error("Failed to track login for user: {}", request.getEmail(), e);
            // Don't fail the login if analytics tracking fails
        }

        // Return only userId in the response body
        return ResponseEntity.ok(Map.of("userId", userId));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/userInfo")
    public ResponseEntity<Map<String, String>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userDetails.getUser();
        Map<String, String> userInfo = Map.of(
                "id", user.getId(),
                "email", user.getEmail()
        );
        return ResponseEntity.ok(userInfo);
    }
}
