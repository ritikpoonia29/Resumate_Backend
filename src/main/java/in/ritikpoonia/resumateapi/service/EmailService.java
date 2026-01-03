package in.ritikpoonia.resumateapi.service;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${email.sender}")
    private String senderEmail;

    @Value("${email.sender.name}")
    private String senderName;

    private static final String BREVO_URL =
            "https://api.brevo.com/v3/smtp/email";

    /* ---------------- SEND HTML EMAIL ---------------- */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            HttpHeaders headers = buildHeaders();

            Map<String, Object> payload = basePayload(to, subject);
            payload.put("htmlContent", htmlContent);

            send(payload, headers);
            log.info("HTML email sent successfully to {}", to);

        } catch (Exception e) {
            log.error("Failed to send HTML email", e);
            throw new RuntimeException("Failed to send email");
        }
    }

    /* ---------------- SEND EMAIL WITH ATTACHMENT ---------------- */
    public void sendEmailWithAttachment(
            String to,
            String subject,
            String body,
            byte[] attachment,
            String filename
    ) {

        try {
            HttpHeaders headers = buildHeaders();

            Map<String, Object> payload = basePayload(to, subject);
            payload.put("textContent", body);

            String encodedFile = Base64.getEncoder().encodeToString(attachment);

            payload.put("attachment", List.of(
                    Map.of(
                            "content", encodedFile,
                            "name", filename
                    )
            ));

            send(payload, headers);
            log.info("Email with attachment sent to {}", to);

        } catch (Exception e) {
            log.error("Failed to send email with attachment", e);
            throw new RuntimeException("Failed to send email with attachment");
        }
    }

    /* ---------------- HELPER METHODS ---------------- */

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        return headers;
    }

    private Map<String, Object> basePayload(String to, String subject) {
        Map<String, Object> payload = new HashMap<>();

        payload.put("sender", Map.of(
                "email", senderEmail,
                "name", senderName
        ));

        payload.put("to", List.of(
                Map.of("email", to)
        ));

        payload.put("subject", subject);
        return payload;
    }

    private void send(Map<String, Object> payload, HttpHeaders headers) {
        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(
                BREVO_URL,
                request,
                String.class
        );
    }
}
