package com.textqr.converter.core;

import com.google.zxing.WriterException;
import com.textqr.converter.model.QRChunk;
import com.textqr.converter.model.QRSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class QRGeneratorTest {
    
    private QRGenerator qrGenerator;
    
    @BeforeEach
    void setUp() {
        qrGenerator = new QRGenerator();
    }
    
    @Test
    void testCreateSessionWithSmallText() {
        String text = "Hello, World!";
        QRSession session = qrGenerator.createSession(text, false);
        
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertEquals(1, session.getChunkCount());
        assertEquals(text, session.getOriginalText());
        assertNotNull(session.getFullChecksum());
    }
    
    @Test
    void testCreateSessionWithLargeText() {
        StringBuilder largeText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            largeText.append("This is line ").append(i).append(" of a very large text file.\n");
        }
        
        QRSession session = qrGenerator.createSession(largeText.toString(), false);
        
        assertNotNull(session);
        assertTrue(session.getChunkCount() > 1);
        assertEquals(largeText.toString(), session.getOriginalText());
    }
    
    @Test
    void testGenerateQRCode() throws WriterException {
        String data = "Test QR Code Data";
        BufferedImage image = qrGenerator.generateQRCode(data, 300);
        
        assertNotNull(image);
        assertEquals(340, image.getWidth()); // Actual size from ZXing + margin
        assertEquals(340, image.getHeight());
    }
    
    @Test
    void testGenerateQRCodeFromChunk() throws WriterException {
        QRChunk chunk = new QRChunk(1, 1, "Test Data", "checksum123", "session123");
        BufferedImage image = qrGenerator.generateQRCode(chunk);
        
        assertNotNull(image);
        assertEquals(540, image.getWidth()); // Actual size from ZXing + margin  
        assertEquals(540, image.getHeight());
    }
    
    @Test
    void testSaveQRCode(@TempDir Path tempDir) throws WriterException, IOException {
        String data = "Test QR Code";
        BufferedImage image = qrGenerator.generateQRCode(data, 200);
        
        File outputFile = tempDir.resolve("test_qr.png").toFile();
        qrGenerator.saveQRCode(image, outputFile);
        
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);
    }
    
    @Test
    void testCreateCompositeImage() throws WriterException {
        BufferedImage qr1 = qrGenerator.generateQRCode("QR 1", 200);
        BufferedImage qr2 = qrGenerator.generateQRCode("QR 2", 200);
        BufferedImage qr3 = qrGenerator.generateQRCode("QR 3", 200);
        BufferedImage qr4 = qrGenerator.generateQRCode("QR 4", 200);
        
        BufferedImage composite = qrGenerator.createCompositeImage(
            java.util.Arrays.asList(qr1, qr2, qr3, qr4), 2
        );
        
        assertNotNull(composite);
        assertTrue(composite.getWidth() > 400); // At least 2 QR codes wide
        assertTrue(composite.getHeight() > 400); // At least 2 QR codes tall
    }
    
    @Test
    void testEmptyTextHandling() {
        String emptyText = "";
        QRSession session = qrGenerator.createSession(emptyText, false);
        
        assertNotNull(session);
        assertEquals(1, session.getChunkCount());
    }
    
    @Test
    void testCompressionEffect() {
        String repetitiveText = "AAAAAAAAAA".repeat(100);
        
        QRSession uncompressed = qrGenerator.createSession(repetitiveText, false);
        QRSession compressed = qrGenerator.createSession(repetitiveText, true);
        
        assertTrue(compressed.getChunkCount() <= uncompressed.getChunkCount());
    }
}