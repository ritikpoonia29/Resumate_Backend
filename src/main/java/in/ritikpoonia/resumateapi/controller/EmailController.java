package in.ritikpoonia.resumateapi.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.ritikpoonia.resumateapi.service.EmailService;
import static in.ritikpoonia.resumateapi.util.AppConstants.EMAIL;
import static in.ritikpoonia.resumateapi.util.AppConstants.SEND_RESUME;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping(EMAIL)
@Slf4j
public class EmailController {

    private final EmailService emailService;

    @PostMapping(
            value = SEND_RESUME,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<Map<String, Object>> sendResumeByEmail(
            @RequestPart("recipientEmail") String recipientEmail,
            @RequestPart(value = "subject", required = false) String subject,
            @RequestPart(value = "message", required = false) String message,
            @RequestPart("pdfFile") MultipartFile pdfFile,
            Authentication authentication
    ) throws IOException {

        Map<String, Object> response = new HashMap<>();

        /* ------------------ Security check ------------------ */
        if (authentication == null || !authentication.isAuthenticated()) {
            response.put("success", false);
            response.put("message", "Unauthorized");
            return ResponseEntity.status(401).body(response);
        }

        /* ------------------ Validation ------------------ */
        if (recipientEmail == null || pdfFile.isEmpty()) {
            response.put("success", false);
            response.put("message", "Missing required fields");
            return ResponseEntity.badRequest().body(response);
        }

        /* ------------------ File handling ------------------ */
        byte[] pdfBytes = pdfFile.getBytes();
        String filename = Objects.requireNonNullElse(
                pdfFile.getOriginalFilename(),
                "resume.pdf"
        );

        /* ------------------ Email content ------------------ */
        String emailSubject =
                Objects.requireNonNullElse(subject, "Resume Application");

        String emailHtml =
                """
                <p>Hello,</p>
                <p>%s</p>
                <p>Please find my resume attached.</p>
                <br/>
                <p>Best regards,<br/><b>ResuMate</b></p>
                """.formatted(
                        Objects.requireNonNullElse(message, "")
                );

        /* ------------------ Send email ------------------ */
        emailService.sendEmailWithAttachment(
                recipientEmail,
                emailSubject,
                emailHtml,
                pdfBytes,
                filename
        );

        /* ------------------ Response ------------------ */
        response.put("success", true);
        response.put("message", "Resume sent successfully to " + recipientEmail);
        return ResponseEntity.ok(response);
    }
}
