package com.textqr.converter.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ChecksumUtilTest {
    
    @Test
    void testCalculateSHA256() {
        String data = "Hello, World!";
        String checksum = ChecksumUtil.calculateSHA256(data);
        
        assertNotNull(checksum);
        assertEquals(64, checksum.length()); // SHA-256 produces 64 hex characters
        
        // Same input should produce same checksum
        String checksum2 = ChecksumUtil.calculateSHA256(data);
        assertEquals(checksum, checksum2);
    }
    
    @Test
    void testCalculateMD5() {
        String data = "Test Data";
        String checksum = ChecksumUtil.calculateMD5(data);
        
        assertNotNull(checksum);
        assertEquals(32, checksum.length()); // MD5 produces 32 hex characters
    }
    
    @Test
    void testVerifyChecksum() {
        String data = "Original Data";
        String checksum = ChecksumUtil.calculateSHA256(data);
        
        assertTrue(ChecksumUtil.verifyChecksum(data, checksum));
        assertFalse(ChecksumUtil.verifyChecksum("Modified Data", checksum));
    }
    
    @Test
    void testCalculateChunkChecksum() {
        int sequenceNumber = 5;
        String data = "Chunk Data";
        
        String checksum1 = ChecksumUtil.calculateChunkChecksum(sequenceNumber, data);
        String checksum2 = ChecksumUtil.calculateChunkChecksum(sequenceNumber, data);
        
        assertEquals(checksum1, checksum2);
        
        // Different sequence number should produce different checksum
        String checksum3 = ChecksumUtil.calculateChunkChecksum(6, data);
        assertNotEquals(checksum1, checksum3);
    }
    
    @Test
    void testEmptyStringChecksum() {
        String emptyData = "";
        String checksum = ChecksumUtil.calculateSHA256(emptyData);
        
        assertNotNull(checksum);
        assertEquals(64, checksum.length());
    }
    
    @Test
    void testUnicodeDataChecksum() {
        String unicodeData = "한글 텍스트 テキスト 🎉";
        String checksum = ChecksumUtil.calculateSHA256(unicodeData);
        
        assertNotNull(checksum);
        assertEquals(64, checksum.length());
    }
}