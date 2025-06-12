package com.textqr.converter.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class CompressionUtil {
    
    public static byte[] compress(byte[] data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            
            gzipOut.write(data);
            gzipOut.finish();
            return baos.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress data", e);
        }
    }
    
    public static byte[] decompress(byte[] compressedData) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipIn = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            
            return baos.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress data", e);
        }
    }
    
    public static double getCompressionRatio(byte[] original, byte[] compressed) {
        return (double) compressed.length / original.length;
    }
}