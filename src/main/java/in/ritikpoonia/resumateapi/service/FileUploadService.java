package in.ritikpoonia.resumateapi.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import in.ritikpoonia.resumateapi.document.Resume;
import in.ritikpoonia.resumateapi.dto.AuthResponse;
import in.ritikpoonia.resumateapi.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileUploadService {

    private final Cloudinary cloudinary;
    private final AuthService authService;
    private final ResumeRepository resumeRepository;

    public Map<String, String> uploadSingleImage(MultipartFile file) throws IOException {
    try {
        long timestamp = System.currentTimeMillis() / 1000;

        Map<String, Object> result = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "image",
                        "timestamp", timestamp
                )
        );  
        System.out.println("imageURL"+result.get("secure_url").toString());
        return Map.of("imageUrl", result.get("secure_url").toString());

    } catch (Exception e) {
    System.out.println("❌❌❌ CLOUDINARY ERROR MESSAGE");
    System.out.println(e.getMessage());

    if (e.getCause() != null) {
        System.out.println("CAUSE: " + e.getCause().getMessage());
    }

    e.printStackTrace(); // IMPORTANT
    throw new RuntimeException(e);
}

}


    public Map<String, String> uploadResumeImages(String resumeId,
                                                  Object principal,
                                                  MultipartFile thumbnail,
                                                  MultipartFile profileImage) throws IOException {
        //Step 1: get the current profile
        AuthResponse response = authService.getProfile(principal);

        //Step 2: get the existing resume
        Resume existingResume = resumeRepository.findByUserIdAndId(response.getId(), resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        //Step 3: upload the resume images and set the resume
        Map<String, String> returnValue = new HashMap<>();
        Map<String, String> uploadResult;

        if (Objects.nonNull(thumbnail)) {
            uploadResult = uploadSingleImage(thumbnail);
            existingResume.setThumbnailLink(uploadResult.get("imageUrl"));
            returnValue.put("thumbnailLink", uploadResult.get("imageUrl"));
        }

        if (Objects.nonNull(profileImage)) {
            uploadResult = uploadSingleImage(profileImage);
            if (Objects.isNull(existingResume.getProfileInfo())) {
                existingResume.setProfileInfo(new Resume.ProfileInfo());
            }
            existingResume.getProfileInfo().setProfilePreviewUrl(uploadResult.get("imageUrl"));
            returnValue.put("profilePreviewUrl", uploadResult.get("imageUrl"));
        }

        //Step 4: update the details into database
        resumeRepository.save(existingResume);
        returnValue.put("message", "Images uploaded successfully");

        //Step 5: return the result
        return returnValue;
    }
}
