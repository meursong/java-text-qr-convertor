package com.textqr.converter.util;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TextChunker {
    private static final int MAX_CHUNK_SIZE = 2953; // QR Code capacity at Level H with alphanumeric
    private static final int METADATA_OVERHEAD = 150; // Reserved for JSON metadata
    private static final int EFFECTIVE_CHUNK_SIZE = MAX_CHUNK_SIZE - METADATA_OVERHEAD;
    
    public static List<String> chunkText(String text, boolean useCompression) {
        List<String> chunks = new ArrayList<>();
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        
        if (useCompression) {
            data = CompressionUtil.compress(data);
        }
        
        String encodedData = Base64.getEncoder().encodeToString(data);
        
        // Handle empty text case - always return at least one chunk
        if (encodedData.isEmpty()) {
            chunks.add("");
            return chunks;
        }
        
        int currentIndex = 0;
        while (currentIndex < encodedData.length()) {
            int endIndex = Math.min(currentIndex + EFFECTIVE_CHUNK_SIZE, encodedData.length());
            chunks.add(encodedData.substring(currentIndex, endIndex));
            currentIndex = endIndex;
        }
        
        return chunks;
    }
    
    public static String reconstructText(List<String> chunks, boolean wasCompressed) {
        StringBuilder combined = new StringBuilder();
        for (String chunk : chunks) {
            combined.append(chunk);
        }
        
        byte[] decodedData = Base64.getDecoder().decode(combined.toString());
        
        if (wasCompressed) {
            decodedData = CompressionUtil.decompress(decodedData);
        }
        
        return new String(decodedData, StandardCharsets.UTF_8);
    }
    
    public static int estimateChunkCount(String text, boolean useCompression) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        
        if (useCompression) {
            data = CompressionUtil.compress(data);
        }
        
        String encodedData = Base64.getEncoder().encodeToString(data);
        return (int) Math.ceil((double) encodedData.length() / EFFECTIVE_CHUNK_SIZE);
    }
}