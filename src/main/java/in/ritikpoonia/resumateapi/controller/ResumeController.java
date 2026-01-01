package in.ritikpoonia.resumateapi.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import in.ritikpoonia.resumateapi.document.Resume;
import in.ritikpoonia.resumateapi.dto.CreateResumeRequest;
import in.ritikpoonia.resumateapi.service.FileUploadService;
import in.ritikpoonia.resumateapi.service.ResumeService;
import static in.ritikpoonia.resumateapi.util.AppConstants.ID;
import static in.ritikpoonia.resumateapi.util.AppConstants.RESUME;
import static in.ritikpoonia.resumateapi.util.AppConstants.UPLOAD_IMAGES;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(RESUME)
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

 @GetMapping("/test")
    public String test() {
        return "CORS working";
    }
    private final ResumeService resumeService;
    private final FileUploadService fileUploadService;

    @PostMapping
    public ResponseEntity<?> createResume(@Valid @RequestBody CreateResumeRequest request,
                                          Authentication authentication) {
        //Step 1: Call the service method
        Resume newResume = resumeService.createResume(request, authentication.getPrincipal());

        //Step 2: return response
        return ResponseEntity.status(HttpStatus.CREATED).body(newResume);
    }

    @GetMapping
    public ResponseEntity<?> getUserResumes(Authentication authentication) {
        //Step 1: Call the service method
        List<Resume> resumes = resumeService.getUserResumes(authentication.getPrincipal());

        //Step 2: return the response
        return ResponseEntity.ok(resumes);
    }

    @GetMapping(ID)
    public ResponseEntity<?> getResumeById(@PathVariable String id,
                                           Authentication authentication) {
        //Step 1: Call the service method
        Resume existingResume = resumeService.getResumeById(id, authentication.getPrincipal());

        //Step 2: return the response;
        return ResponseEntity.ok(existingResume);
    }

    @PutMapping(ID)
    public ResponseEntity<?> updateResume(@PathVariable String id,
                                          @RequestBody Resume updatedData,
                                          Authentication authentication) {
        //Step 1: Call the service method
        Resume updatedResume = resumeService.updateResume(id, updatedData, authentication.getPrincipal());

        //Step 2: return the response
        return ResponseEntity.ok(updatedResume);
    }

    @PutMapping(UPLOAD_IMAGES)
    public ResponseEntity<?> uploadResumeImages(@PathVariable String id,
                                                @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
                                                @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
                                                Authentication authentication) throws IOException {
        //Step 1: call the service method
        Map<String, String> response = fileUploadService.uploadResumeImages(id, authentication.getPrincipal(), thumbnail, profileImage);

        //Step 2: return the response
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(ID)
    public ResponseEntity<?> deleteResume(@PathVariable String id,
                                          Authentication authentication) {
        //Step 1: Call the service method
        resumeService.deleteResume(id, authentication.getPrincipal());

        //Step 2: return response
        return ResponseEntity.ok(Map.of("message", "Resume deleted successfully"));
    }

}
