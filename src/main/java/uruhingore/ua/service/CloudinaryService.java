package uruhingore.ua.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload a profile photo to Cloudinary
     * @param file The image file to upload
     * @param studentId The student ID to use in the public ID
     * @return The URL of the uploaded image
     * @throws IOException if upload fails
     */
    public String uploadProfilePhoto(MultipartFile file, UUID studentId) throws IOException {
        log.info("Uploading profile photo for student: {}", studentId);

        // Validate file
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        // Validate file size (max 10MB)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 10MB");
        }

        // Read file bytes first (before validating image to avoid stream consumption issues)
        byte[] fileBytes = file.getBytes();

        // Validate file type - check if it's actually an image using the bytes
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));
        if (image == null) {
            throw new IllegalArgumentException("Invalid file format. Only image files are allowed.");
        }

        try {
            // Upload to Cloudinary with specific folder and public ID
            // Pass transformation parameters directly in upload params
            Map<String, Object> uploadParams = ObjectUtils.asMap(
                    "folder", "student-profiles",
                    "public_id", "student_" + studentId,
                    "overwrite", true,
                    "resource_type", "image",
                    "use_filename", true,
                    "unique_filename", false,
                    "width", 500,
                    "height", 500,
                    "crop", "fill",
                    "gravity", "face",
                    "quality", "auto"
            );

            Map<?, ?> uploadResult = cloudinary.uploader().upload(fileBytes, uploadParams);
            String imageUrl = (String) uploadResult.get("secure_url");

            log.info("Profile photo uploaded successfully. URL: {}", imageUrl);
            return imageUrl;
        } catch (IOException e) {
            log.error("Error uploading profile photo to Cloudinary", e);
            throw new IOException("Failed to upload profile photo: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a profile photo from Cloudinary
     * @param imageUrl The URL of the image to delete
     * @throws IOException if deletion fails
     */
    public void deleteProfilePhoto(String imageUrl) throws IOException {
        deleteFile(imageUrl);
    }

    /**
     * Delete a file from Cloudinary (tries different resource types)
     * @param fileUrl The URL of the file to delete
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl != null && !fileUrl.isEmpty()) {
                String publicId = extractPublicIdFromUrl(fileUrl);
                if (publicId == null) {
                    log.warn("Could not extract public ID from URL: {}", fileUrl);
                    return;
                }
                
                // Try to delete as image first (most common)
                try {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));
                    log.info("File deleted successfully as image. Public ID: {}", publicId);
                } catch (Exception imageEx) {
                    // If image deletion fails, try video
                    try {
                        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
                        log.info("File deleted successfully as video. Public ID: {}", publicId);
                    } catch (Exception videoEx) {
                        // If video deletion fails, try raw (documents)
                        try {
                            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
                            log.info("File deleted successfully as raw. Public ID: {}", publicId);
                        } catch (Exception rawEx) {
                            // If all fail, try without resource type
                            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                            log.info("File deleted successfully without resource type. Public ID: {}", publicId);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete file from Cloudinary: {}", fileUrl, e);
            throw new RuntimeException("Failed to delete file from Cloudinary", e);
        }
    }

    /**
     * Extract public ID from Cloudinary URL
     */
    private String extractPublicIdFromUrl(String url) {
        try {
            // Remove query parameters if they exist
            String cleanUrl = url.split("\\?")[0];
            
            // Handle different Cloudinary URL formats:
            // - /image/upload/{version}/{public_id}.{format}
            // - /image/upload/{public_id}.{format}
            // - /video/upload/{public_id}.{format}
            // - /raw/upload/{public_id}.{format}
            String[] resourceTypes = {"/image/upload/", "/video/upload/", "/raw/upload/"};
            
            for (String resourceType : resourceTypes) {
                if (cleanUrl.contains(resourceType)) {
                    String[] parts = cleanUrl.split(resourceType);
                    if (parts.length > 1) {
                        String path = parts[1];
                        // Remove version if present (format: v1234567890/public_id.format)
                        if (path.contains("/")) {
                            path = path.substring(path.indexOf("/") + 1);
                        }
                        // Remove file extension
                        int lastDot = path.lastIndexOf(".");
                        if (lastDot > 0) {
                            path = path.substring(0, lastDot);
                        }
                        return path;
                    }
                }
            }
            
            // Fallback: try to extract from the last part of URL
            String[] parts = cleanUrl.split("/");
            String filenameWithExtension = parts[parts.length - 1];
            int lastDotIndex = filenameWithExtension.lastIndexOf('.');
            if (lastDotIndex > 0) {
                return filenameWithExtension.substring(0, lastDotIndex);
            } else {
                return filenameWithExtension; // No extension found
            }
        } catch (Exception e) {
            log.warn("Could not extract public ID from URL: {}", url, e);
            // Fallback: try to extract from the last part of URL
            try {
                String[] parts = url.split("/");
                return parts[parts.length - 1];
            } catch (Exception ex) {
                log.error("Failed to extract public ID from URL: {}", url, ex);
                return null;
            }
        }
    }
}

