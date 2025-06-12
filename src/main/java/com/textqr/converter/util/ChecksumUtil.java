package com.textqr.converter.util;

import org.apache.commons.codec.digest.DigestUtils;
import java.nio.charset.StandardCharsets;

public class ChecksumUtil {
    
    public static String calculateSHA256(String data) {
        return DigestUtils.sha256Hex(data.getBytes(StandardCharsets.UTF_8));
    }
    
    public static String calculateMD5(String data) {
        return DigestUtils.md5Hex(data.getBytes(StandardCharsets.UTF_8));
    }
    
    public static boolean verifyChecksum(String data, String expectedChecksum) {
        String actualChecksum = calculateSHA256(data);
        return actualChecksum.equals(expectedChecksum);
    }
    
    public static String calculateChunkChecksum(int sequenceNumber, String data) {
        String combined = sequenceNumber + ":" + data;
        return calculateMD5(combined);
    }
}