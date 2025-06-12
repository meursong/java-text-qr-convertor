package com.textqr.converter.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TextChunkerTest {
    
    @Test
    void testChunkSmallText() {
        String smallText = "This is a small text that fits in one chunk.";
        List<String> chunks = TextChunker.chunkText(smallText, false);
        
        assertEquals(1, chunks.size());
        assertFalse(chunks.get(0).isEmpty());
    }
    
    @Test
    void testChunkLargeText() {
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 500; i++) {
            largeText.append("This is line ").append(i).append(" of a large text file. ");
        }
        
        List<String> chunks = TextChunker.chunkText(largeText.toString(), false);
        
        assertTrue(chunks.size() > 1);
        
        // Each chunk should not exceed maximum size
        for (String chunk : chunks) {
            assertTrue(chunk.length() <= 2803); // MAX_CHUNK_SIZE - METADATA_OVERHEAD
        }
    }
    
    @Test
    void testReconstructText() {
        String originalText = "This is the original text that will be chunked and reconstructed.";
        
        List<String> chunks = TextChunker.chunkText(originalText, false);
        String reconstructed = TextChunker.reconstructText(chunks, false);
        
        assertEquals(originalText, reconstructed);
    }
    
    @Test
    void testReconstructTextWithCompression() {
        String originalText = "This is a text with repetitive patterns. " +
                            "This is a text with repetitive patterns. " +
                            "This is a text with repetitive patterns.";
        
        List<String> chunks = TextChunker.chunkText(originalText, true);
        String reconstructed = TextChunker.reconstructText(chunks, true);
        
        assertEquals(originalText, reconstructed);
    }
    
    @Test
    void testEstimateChunkCount() {
        String text = "Sample text";
        
        int estimatedCount = TextChunker.estimateChunkCount(text, false);
        List<String> actualChunks = TextChunker.chunkText(text, false);
        
        assertEquals(actualChunks.size(), estimatedCount);
    }
    
    @Test
    void testCompressionReducesChunks() {
        StringBuilder repetitiveText = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            repetitiveText.append("AAAAAAAAAA");
        }
        
        List<String> uncompressedChunks = TextChunker.chunkText(repetitiveText.toString(), false);
        List<String> compressedChunks = TextChunker.chunkText(repetitiveText.toString(), true);
        
        assertTrue(compressedChunks.size() <= uncompressedChunks.size());
    }
    
    @Test
    void testUnicodeTextChunking() {
        String unicodeText = "한글 텍스트 " + "テキスト " + "🎉🎊🎈 " + "Text with emojis";
        
        List<String> chunks = TextChunker.chunkText(unicodeText, false);
        String reconstructed = TextChunker.reconstructText(chunks, false);
        
        assertEquals(unicodeText, reconstructed);
    }
    
    @Test
    void testEmptyTextChunking() {
        String emptyText = "";
        
        List<String> chunks = TextChunker.chunkText(emptyText, false);
        
        assertEquals(1, chunks.size());
        
        String reconstructed = TextChunker.reconstructText(chunks, false);
        assertEquals(emptyText, reconstructed);
    }
}