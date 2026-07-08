package chat_ai.controller;



import chat_ai.entity.Attachment;
import chat_ai.repository.AttachmentRepository;
import chat_ai.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final FileStorageService fileStorageService;
    private final AttachmentRepository attachmentRepository;

    @PostMapping("/upload")
    public ResponseEntity<Attachment> uploadFile(@RequestParam("file") MultipartFile file) {

        // 1. File ko validate karo (size/type check - optional but recommended)
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        // 2. File ko local storage me save karo
        String filePath = fileStorageService.storeFile(file);

        // 3. Attachment record banao (abhi message se linked nahi hai, wo ChatService me hoga)
        Attachment attachment = new Attachment();
        attachment.setFileName(file.getOriginalFilename());
        attachment.setFileType(file.getContentType());
        attachment.setFilePath(filePath);
        attachment.setFileSize(file.getSize());

        attachment = attachmentRepository.save(attachment);

        return ResponseEntity.ok(attachment);
    }
    @GetMapping("/download/{attachmentId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadFile(@PathVariable Long attachmentId) throws java.io.IOException {

        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new RuntimeException("Attachment not found"));

        java.nio.file.Path filePath = java.nio.file.Paths.get(attachment.getFilePath());
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.parseMediaType(attachment.getFileType()))
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + attachment.getFileName() + "\"")
                .body(resource);
    }
}
