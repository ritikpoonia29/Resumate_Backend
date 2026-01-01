package in.ritikpoonia.resumateapi.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import in.ritikpoonia.resumateapi.document.User;
import in.ritikpoonia.resumateapi.dto.AuthResponse;
import in.ritikpoonia.resumateapi.dto.LoginRequest;
import in.ritikpoonia.resumateapi.dto.RegisterRequest;
import in.ritikpoonia.resumateapi.exception.ResourceExistsException;
import in.ritikpoonia.resumateapi.repository.UserRepository;
import in.ritikpoonia.resumateapi.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${app.base.url:http://localhost:8080}")
    private String appBaseUrl;

    public AuthResponse register(RegisterRequest request) {
        log.info("Inside AuthService: register() {} ", request);

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResourceExistsException("User already exists with this email");
        }

        User newUser = toDocument(request);

        userRepository.save(newUser);

        sendVerificationEmail(newUser);

        return toResponse(newUser);
    }

    private void sendVerificationEmail(User newUser) {
        log.info("Inside AuthService - sendVerificationEmail(): {}", newUser);
        try {
            String link = appBaseUrl+"/api/auth/verify-email?token="+newUser.getVerificationToken();
            String html = "<div style='font-family:sans-serif'>" +
                    "<h2>Verify your email</h2>" +
                    "<p>Hi " + newUser.getName() + ", please confirm your email to activate your account.</p>" +
                    "<p><a href='" + link
                    + "' style='display:inline-block;padding:10px 16px;background:#6366f1;color:#fff;border-radius:6px;text-decoration:none'>Verify Email</a></p>"
                    +
                    "<p>Or copy this link: " + link + "</p>" +
                    "<p>This link expires in 24 hours.</p>" +
                    "</div>";
            emailService.sendHtmlEmail(newUser.getEmail(), "Verify your email", html);
        }catch (Exception e) {
            log.error("Exception occured at sendVerificationEmail(): {}", e.getMessage());
            throw new RuntimeException("Failed to send verification email: "+e.getMessage());
        }
    }

    private AuthResponse toResponse(User newUser) {
        return AuthResponse.builder()
                .id(newUser.getId())
                .name(newUser.getName())
                .email(newUser.getEmail())
                .profileImageUrl(newUser.getProfileImageUrl())
                .emailVerified(newUser.isEmailVerified())
                .subscriptionPlan(newUser.getSubscriptionPlan())
                .createdAt(newUser.getCreatedAt())
                .updatedAt(newUser.getUpdatedAt())
                .build();
    }

    private User toDocument(RegisterRequest request) {
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profileImageUrl(request.getProfileImageUrl())
                .subscriptionPlan("Basic")
                .emailVerified(false)
                .verificationToken(UUID.randomUUID().toString())
                .verificationExpires(LocalDateTime.now().plusHours(24))
                .build();
    }

    public void verifyEmail(String token) {
        log.info("Inside AuthService: verifyEmail(): {}", token);
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        if (user.getVerificationExpires() != null && user.getVerificationExpires().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired. Please request new one.");
        }

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        user.setVerificationExpires(null);
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        User existingUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())) {
            throw new UsernameNotFoundException("Invalid email or password");
        }

        if (!existingUser.isEmailVerified()) {
            throw new RuntimeException("Please verify your email before logging in.");
        }

        String token = jwtUtil.generateToken(existingUser.getId());

        AuthResponse response = toResponse(existingUser);
        response.setToken(token);
        return response;

    }

    public void resendVerification(String email) {
        //Step 1: Fetch the user account by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        //Step 2: Check the email is verified
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified.");
        }

        //Step 3: Set the new verification token and expires time
        user.setVerificationToken(UUID.randomUUID().toString());
        user.setVerificationExpires(LocalDateTime.now().plusHours(24));

        //Step 4: Update the user
        userRepository.save(user);

        //Step 5: Resend the verification email
        sendVerificationEmail(user);

    }

    public AuthResponse getProfile(Object principalObject) {
        User existingUser = (User) principalObject;
        return toResponse(existingUser);
    }
}
