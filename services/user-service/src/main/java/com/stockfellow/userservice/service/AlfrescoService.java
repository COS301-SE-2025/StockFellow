package com.stockfellow.userservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;

@Service
public class AlfrescoService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlfrescoService.class);
    
    private final RestTemplate restTemplate;
    
    @Value("${alfresco.base-url:http://localhost:8081/alfresco}")
    private String alfrescoBaseUrl;
    
    @Value("${alfresco.username:admin}")
    private String alfrescoUsername;
    
    @Value("${alfresco.password:admin}")
    private String alfrescoPassword;
    
    @Value("${alfresco.site-name:stockfellow}")
    private String siteName;
    
    private String authToken;
    
    public AlfrescoService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    @PostConstruct
    public void initialize() {
        try {
            authenticateWithAlfresco();
            logger.info("AlfrescoService initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to authenticate with Alfresco during initialization", e);
        }
    }
    
    /**
     * Authenticates with Alfresco and stores the auth token
     */
    private void authenticateWithAlfresco() {
        try {
            String loginUrl = alfrescoBaseUrl + "/api/-default-/public/authentication/versions/1/tickets";
            
            Map<String, Object> loginRequest = Map.of(
                "userId", alfrescoUsername,
                "password", alfrescoPassword
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(loginRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> entry = (Map<String, Object>) response.getBody().get("entry");
                if (entry != null) {
                    this.authToken = (String) entry.get("id");
                    logger.info("Successfully authenticated with Alfresco");
                } else {
                    logger.error("Authentication response missing entry data");
                    throw new RuntimeException("Invalid authentication response from Alfresco");
                }
            } else {
                logger.error("Failed to authenticate with Alfresco. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to authenticate with Alfresco");
            }
            
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error during Alfresco authentication: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during Alfresco authentication", e);
            throw new RuntimeException("Unexpected authentication error: " + e.getMessage());
        }
    }
    
    /**
     * Uploads a document to Alfresco with retry logic
     * @param file The file to upload
     * @param userId The user ID for organizing documents
     * @param documentType The type of document (e.g., "ID_VERIFICATION")
     * @return The document ID in Alfresco
     */
    public String uploadDocument(MultipartFile file, String userId, String documentType) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }
        
        if (userId == null || userId.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        
        // Retry authentication if needed
        if (authToken == null) {
            authenticateWithAlfresco();
            if (authToken == null) {
                throw new RuntimeException("Cannot authenticate with Alfresco");
            }
        }
        
        try {
            // Create user folder if it doesn't exist
            String userFolderId = createUserFolder(userId);
            
            // Upload the document
            String documentId = uploadFileToFolder(file, userFolderId, documentType, userId);
            
            logger.info("Document uploaded successfully to Alfresco. DocumentId: {}, UserId: {}, Type: {}", 
                       documentId, userId, documentType);
            
            return documentId;
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                logger.warn("Authentication expired, retrying...");
                // Retry with new authentication
                authenticateWithAlfresco();
                return uploadDocument(file, userId, documentType); // Recursive call
            } else {
                logger.error("HTTP client error uploading document to Alfresco: {} - {}", 
                           e.getStatusCode(), e.getResponseBodyAsString());
                throw new IOException("Failed to upload document: " + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error uploading document to Alfresco for user: {}", userId, e);
            throw new IOException("Failed to upload document to Alfresco: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a user-specific folder in Alfresco
     */
    private String createUserFolder(String userId) {
        try {
            // First, get the document library folder ID
            String documentLibraryId = getDocumentLibraryId();
            
            // Check if user folder already exists
            String existingFolderId = findUserFolder(documentLibraryId, userId);
            if (existingFolderId != null) {
                logger.debug("User folder already exists for user: {}", userId);
                return existingFolderId;
            }
            
            // Create new user folder
            String createFolderUrl = alfrescoBaseUrl + "/api/-default-/public/alfresco/versions/1/nodes/" + 
                                   documentLibraryId + "/children";
            
            Map<String, Object> folderRequest = Map.of(
                "name", "user_" + userId,
                "nodeType", "cm:folder",
                "properties", Map.of(
                    "cm:title", "Documents for User " + userId,
                    "cm:description", "Document storage for user ID: " + userId + " created on " + 
                                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                )
            );
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                (alfrescoUsername + ":" + alfrescoPassword).getBytes()));
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(folderRequest, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(createFolderUrl, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> entry = (Map<String, Object>) response.getBody().get("entry");
                String folderId = (String) entry.get("id");
                logger.info("Created user folder: {} for user: {}", folderId, userId);
                return folderId;
            } else {
                logger.error("Failed to create user folder. Status: {}, Response: {}", 
                           response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to create user folder in Alfresco");
            }
            
        } catch (Exception e) {
            logger.error("Error creating user folder for userId: {}", userId, e);
            throw new RuntimeException("Failed to create user folder: " + e.getMessage(), e);
        }
    }
    
    /**
     * Gets the document library folder ID for the site
     */
    private String getDocumentLibraryId() {
        try {
            String siteUrl = alfrescoBaseUrl + "/api/-default-/public/alfresco/versions/1/sites/" + 
                           siteName + "/containers";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                (alfrescoUsername + ":" + alfrescoPassword).getBytes()));
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(siteUrl, HttpMethod.GET, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> list = (Map<String, Object>) response.getBody().get("list");
                if (list != null) {
                    Object[] entries = (Object[]) list.get("entries");
                    if (entries != null) {
                        for (Object entryObj : entries) {
                            Map<String, Object> entry = (Map<String, Object>) ((Map<String, Object>) entryObj).get("entry");
                            if ("documentLibrary".equals(entry.get("folderId"))) {
                                return (String) entry.get("id");
                            }
                        }
                    }
                }
            }
            
            // If document library not found, use a default approach
            logger.warn("Document library not found for site: {}, using root folder", siteName);
            return "-root-"; // Alfresco root folder
            
        } catch (Exception e) {
            logger.error("Error getting document library ID for site: {}", siteName, e);
            return "-root-";
        }
    }
    
    /**
     * Finds an existing user folder
     */
    private String findUserFolder(String parentFolderId, String userId) {
        try {
            String searchUrl = alfrescoBaseUrl + "/api/-default-/public/alfresco/versions/1/nodes/" + 
                             parentFolderId + "/children?where=(name='user_" + userId + "')";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                (alfrescoUsername + ":" + alfrescoPassword).getBytes()));
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(searchUrl, HttpMethod.GET, request, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> list = (Map<String, Object>) response.getBody().get("list");
                if (list != null) {
                    Object[] entries = (Object[]) list.get("entries");
                    if (entries != null && entries.length > 0) {
                        Map<String, Object> entry = (Map<String, Object>) ((Map<String, Object>) entries[0]).get("entry");
                        return (String) entry.get("id");
                    }
                }
            }
            
            return null;
            
        } catch (Exception e) {
            logger.error("Error finding user folder for userId: {}", userId, e);
            return null;
        }
    }
    
    /**
     * Uploads a file to a specific folder
     */
    private String uploadFileToFolder(MultipartFile file, String folderId, String documentType, String userId) throws IOException {
        String uploadUrl = alfrescoBaseUrl + "/api/-default-/public/alfresco/versions/1/nodes/" + 
                         folderId + "/children";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
            (alfrescoUsername + ":" + alfrescoPassword).getBytes()));
        
        // Create multipart body
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        
        // Add file
        ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return generateFileName(file.getOriginalFilename(), documentType, userId);
            }
        };
        
        body.add("filedata", fileResource);
        
        // Add properties
        String properties = String.format(
            "{\"cm:title\":\"%s\",\"cm:description\":\"Document type: %s, User: %s, Uploaded: %s\"}",
            documentType + " Document for User " + userId,
            documentType,
            userId,
            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
        body.add("properties", properties);
        
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(uploadUrl, requestEntity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> entry = (Map<String, Object>) response.getBody().get("entry");
                String documentId = (String) entry.get("id");
                logger.info("File uploaded successfully to Alfresco: {}", documentId);
                return documentId;
            } else {
                throw new IOException("Failed to upload file to Alfresco. Status: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.error("HTTP error uploading file: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IOException("Failed to upload file: " + e.getMessage());
        }
    }
    
    /**
     * Generates a unique filename for the document
     */
    private String generateFileName(String originalFilename, String documentType, String userId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = ".pdf"; // Default extension
        
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        return String.format("%s_%s_%s%s", documentType, userId, timestamp, extension);
    }
    
    /**
     * Downloads a document from Alfresco
     */
    public byte[] downloadDocument(String documentId) throws IOException {
        try {
            String downloadUrl = alfrescoBaseUrl + "/api/-default-/public/alfresco/versions/1/nodes/" + 
                                documentId + "/content";
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(
                (alfrescoUsername + ":" + alfrescoPassword).getBytes()));
            
            HttpEntity<String> request = new HttpEntity<>(headers);
            
            ResponseEntity<byte[]> response = restTemplate.exchange(downloadUrl, HttpMethod.GET, request, byte[].class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Document downloaded successfully: {}", documentId);
                return response.getBody();
            } else {
                throw new IOException("Failed to download document from Alfresco. Status: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            logger.error("Error downloading document from Alfresco. DocumentId: {}", documentId, e);            throw new IOException("Failed to download document", e);
        }
    }
}

