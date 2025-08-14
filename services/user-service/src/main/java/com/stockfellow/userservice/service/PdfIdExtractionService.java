package com.stockfellow.userservice.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

@Service
public class PdfIdExtractionService {
    
    // Pattern to match South African ID numbers (13 digits)
    private static final Pattern SA_ID_PATTERN = Pattern.compile("\\b(\\d{13})\\b");
    
    // Common variations of ID number labels in SA documents
    private static final Pattern ID_LABEL_PATTERN = Pattern.compile(
        "(?i)(identity\\s*number|id\\s*number|id\\s*no|identity\\s*no|rsa\\s*id|sa\\s*id)\\s*:?\\s*(\\d{13})"
    );
    
    /**
     * Extracts ID number from a PDF file
     * @param pdfFile The uploaded PDF file
     * @return The extracted 13-digit ID number, or null if not found
     * @throws IOException if there's an error reading the PDF
     */
    public String extractIdNumberFromPdf(MultipartFile pdfFile) throws IOException {
        if (pdfFile == null || pdfFile.isEmpty()) {
            throw new IllegalArgumentException("PDF file is required");
        }
        
        try (PDDocument document = PDDocument.load(pdfFile.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            
            return extractIdFromText(text);
        }
    }
    
    /**
     * Extracts ID number from text content
     * @param text The text content to search
     * @return The extracted 13-digit ID number, or null if not found
     */
    public String extractIdFromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        
        // Clean the text - remove extra whitespace and normalize
        String cleanedText = text.replaceAll("\\s+", " ").trim();
        
        // First try to find ID with explicit labels
        String idWithLabel = findIdWithLabel(cleanedText);
        if (idWithLabel != null) {
            return idWithLabel;
        }
        
        // If no labeled ID found, search for any 13-digit number
        return findAnyValidId(cleanedText);
    }
    
    /**
     * Searches for ID numbers with explicit labels
     */
    private String findIdWithLabel(String text) {
        Matcher matcher = ID_LABEL_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(2);
        }
        return null;
    }
    
    /**
     * Searches for any valid 13-digit number that could be an ID
     */
    private String findAnyValidId(String text) {
        List<String> candidateIds = new ArrayList<>();
        Matcher matcher = SA_ID_PATTERN.matcher(text);
        
        while (matcher.find()) {
            candidateIds.add(matcher.group(1));
        }
        
        // If only one candidate found, return it
        if (candidateIds.size() == 1) {
            return candidateIds.get(0);
        }
        
        // If multiple candidates, try to determine the most likely one
        if (candidateIds.size() > 1) {
            return selectMostLikelyId(candidateIds, text);
        }
        
        return null;
    }
    
    /**
     * Selects the most likely ID from multiple candidates
     * This uses heuristics like position in document, surrounding context, etc.
     */
    private String selectMostLikelyId(List<String> candidates, String text) {
        // Simple heuristic: return the first one that appears near common ID-related keywords
        String[] idKeywords = {"identity", "id", "number", "citizen", "document", "card"};
        
        for (String candidate : candidates) {
            int position = text.indexOf(candidate);
            if (position > 0) {
                // Check text before and after the candidate
                int start = Math.max(0, position - 50);
                int end = Math.min(text.length(), position + candidate.length() + 50);
                String context = text.substring(start, end).toLowerCase();
                
                for (String keyword : idKeywords) {
                    if (context.contains(keyword)) {
                        return candidate;
                    }
                }
            }
        }
        
        // If no contextual match, return the first candidate
        return candidates.get(0);
    }
    
    /**
     * Extracts multiple ID numbers from text (useful for documents containing multiple IDs)
     */
    public List<String> extractAllIdNumbers(String text) {
        List<String> ids = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return ids;
        }
        
        Matcher matcher = SA_ID_PATTERN.matcher(text);
        while (matcher.find()) {
            ids.add(matcher.group(1));
        }
        
        return ids;
    }
    
    /**
     * Validates if the extracted content looks like a South African ID document
     */
    public boolean isLikelySaIdDocument(String text) {
        if (text == null) {
            return false;
        }
        
        String lowerText = text.toLowerCase();
        
        // Check for common SA ID document indicators
        String[] saIdIndicators = {
            "republic of south africa",
            "south african identity",
            "identity document",
            "rsa id",
            "department of home affairs",
            "identity number"
        };
        
        for (String indicator : saIdIndicators) {
            if (lowerText.contains(indicator)) {
                return true;
            }
        }
        
        return false;
    }
}
