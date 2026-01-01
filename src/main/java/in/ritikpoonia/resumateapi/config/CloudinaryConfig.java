package in.ritikpoonia.resumateapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloudinary.Cloudinary;

@ConditionalOnProperty(
        name = "cloudinary.enabled",
        havingValue = "true",
        matchIfMissing = false
)
@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        String cloudinaryUrl =
                "cloudinary://" + apiKey + ":" + apiSecret + "@" + cloudName;

        return new Cloudinary(cloudinaryUrl);
    }
}
