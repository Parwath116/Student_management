package com.parwath.sms.controller;

import com.parwath.sms.dto.AuthRequest;
import com.parwath.sms.dto.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest authRequest,
            HttpServletRequest request
    ) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            SecurityContext securityContext = SecurityContextHolder.getContext();
            securityContext.setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    securityContext
            );

            return ResponseEntity.ok(AuthResponse.builder()
                    .authenticated(true)
                    .username(authentication.getName())
                    .role(authentication.getAuthorities().iterator().next().getAuthority())
                    .message("Authentication successful")
                    .build());
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .authenticated(false)
                            .message("Invalid username or password")
                            .build());
        }
    }

    @GetMapping("/status")
    public ResponseEntity<AuthResponse> status(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.ok(AuthResponse.builder()
                    .authenticated(true)
                    .username(authentication.getName())
                    .role(authentication.getAuthorities().isEmpty() ? "ROLE_USER" : authentication.getAuthorities().iterator().next().getAuthority())
                    .message("Authenticated")
                    .build());
        }
        return ResponseEntity.ok(AuthResponse.builder()
                .authenticated(false)
                .message("Not authenticated")
                .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(AuthResponse.builder()
                .authenticated(false)
                .message("Logged out successfully")
                .build());
    }
}
