package in.ritikpoonia.resumateapi.controller;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.ritikpoonia.resumateapi.dto.AuthResponse;
import in.ritikpoonia.resumateapi.dto.LoginRequest;
import in.ritikpoonia.resumateapi.dto.RegisterRequest;
import in.ritikpoonia.resumateapi.service.AuthService;
import in.ritikpoonia.resumateapi.service.FileUploadService;
import static in.ritikpoonia.resumateapi.util.AppConstants.AUTH_CONTROLLER;
import static in.ritikpoonia.resumateapi.util.AppConstants.LOGIN;
import static in.ritikpoonia.resumateapi.util.AppConstants.PROFILE;
import static in.ritikpoonia.resumateapi.util.AppConstants.REGISTER;
import static in.ritikpoonia.resumateapi.util.AppConstants.RESEND_VERIFICATION;
import static in.ritikpoonia.resumateapi.util.AppConstants.UPLOAD_PROFILE;
import static in.ritikpoonia.resumateapi.util.AppConstants.VERIFY_EMAIL;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping(AUTH_CONTROLLER)
public class AuthController {

    @Value("${app.home.url}")
    private String homePage;
    private final AuthService authService;
    private final FileUploadService fileUploadService;
    

    @PostMapping(REGISTER)
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
    log.info("Inside AuthController - register(): {}", request);
        AuthResponse response = authService.register(request);
        log.info("Response from service: {}", response);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(VERIFY_EMAIL)
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        log.info("Inside AuthController - verifyEmail(): {}", token);
        authService.verifyEmail(token);
        return ResponseEntity.status(HttpStatus.FOUND).header(
            "location",
            homePage + "/?emailVerified=success"
        ).build();
    }

    @PostMapping(UPLOAD_PROFILE)
public ResponseEntity<?> uploadImage(@RequestPart("image") MultipartFile file) throws IOException {
    System.out.println("🔥🔥🔥 CONTROLLER HIT");
    log.error("🔥🔥🔥 CONTROLLER HIT");

    Map<String, String> response = fileUploadService.uploadSingleImage(file);
    return ResponseEntity.ok(response);
}

    @PostMapping(LOGIN)
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(RESEND_VERIFICATION)
    public ResponseEntity<?> resendVerification(@RequestBody Map<String, String> body) {
        //Step 1: Get the email from request
        String email = body.get("email");

        //Step 2: Add the validations
        if (Objects.isNull(email)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required"));
        }

        //Step 3: Call the service method to resend verification link
        authService.resendVerification(email);

        //Step 4: Return response
        return ResponseEntity.ok(Map.of("success", true, "message", "Verification email sent"));
    }

    @GetMapping(PROFILE)
    public ResponseEntity<?> getProfile(Authentication authentication) {
        //Step 1: Get the principal object
        Object principalObject = authentication.getPrincipal();

        //Step 2: Call the service method
        AuthResponse currentProfile = authService.getProfile(principalObject);

        //Step 3: return the response
        return ResponseEntity.ok(currentProfile);
    }

    public String getHomePage() {
        return homePage;
    }

}
