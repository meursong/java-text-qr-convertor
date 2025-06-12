package com.textqr.converter.core;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.textqr.converter.model.QRChunk;
import com.textqr.converter.model.QRSession;
import com.textqr.converter.util.ChecksumUtil;
import com.textqr.converter.util.TextChunker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QRGenerator {
    private static final Logger logger = LoggerFactory.getLogger(QRGenerator.class);
    
    private static final int DEFAULT_QR_SIZE = 500;
    private static final int MARGIN = 20;
    private static final ErrorCorrectionLevel ERROR_CORRECTION = ErrorCorrectionLevel.H;
    
    private final QRCodeWriter qrCodeWriter;
    
    public QRGenerator() {
        this.qrCodeWriter = new QRCodeWriter();
    }
    
    public QRSession createSession(String text, boolean useCompression) {
        logger.info("Creating QR session for text of length: {}", text.length());
        
        String fullChecksum = ChecksumUtil.calculateSHA256(text);
        QRSession session = new QRSession(text, fullChecksum);
        
        List<String> chunks = TextChunker.chunkText(text, useCompression);
        logger.info("Text split into {} chunks", chunks.size());
        
        for (int i = 0; i < chunks.size(); i++) {
            String chunkData = chunks.get(i);
            String chunkChecksum = ChecksumUtil.calculateChunkChecksum(i + 1, chunkData);
            
            QRChunk chunk = new QRChunk(
                i + 1,
                chunks.size(),
                chunkData,
                chunkChecksum,
                session.getSessionId()
            );
            
            session.addChunk(chunk);
        }
        
        return session;
    }
    
    public BufferedImage generateQRCode(QRChunk chunk) throws WriterException {
        return generateQRCode(chunk.toJsonString(), DEFAULT_QR_SIZE);
    }
    
    public BufferedImage generateQRCode(String data, int size) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ERROR_CORRECTION);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);
        
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, size, size, hints);
        
        return createQRImage(bitMatrix);
    }
    
    private BufferedImage createQRImage(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        
        BufferedImage image = new BufferedImage(
            width + (MARGIN * 2), 
            height + (MARGIN * 2), 
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fill background
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        
        // Draw QR code
        graphics.setColor(Color.BLACK);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (bitMatrix.get(x, y)) {
                    graphics.fillRect(x + MARGIN, y + MARGIN, 1, 1);
                }
            }
        }
        
        graphics.dispose();
        return image;
    }
    
    public void saveQRCode(BufferedImage image, File outputFile) throws IOException {
        ImageIO.write(image, "PNG", outputFile);
        logger.info("QR code saved to: {}", outputFile.getAbsolutePath());
    }
    
    public BufferedImage createCompositeImage(List<BufferedImage> qrCodes, int columns) {
        if (qrCodes.isEmpty()) {
            throw new IllegalArgumentException("No QR codes to compose");
        }
        
        int qrSize = qrCodes.get(0).getWidth();
        int rows = (int) Math.ceil((double) qrCodes.size() / columns);
        
        int compositeWidth = columns * qrSize + (columns - 1) * MARGIN;
        int compositeHeight = rows * qrSize + (rows - 1) * MARGIN;
        
        BufferedImage composite = new BufferedImage(
            compositeWidth + (MARGIN * 2),
            compositeHeight + (MARGIN * 2),
            BufferedImage.TYPE_INT_RGB
        );
        
        Graphics2D g = composite.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, composite.getWidth(), composite.getHeight());
        
        for (int i = 0; i < qrCodes.size(); i++) {
            int row = i / columns;
            int col = i % columns;
            
            int x = MARGIN + col * (qrSize + MARGIN);
            int y = MARGIN + row * (qrSize + MARGIN);
            
            g.drawImage(qrCodes.get(i), x, y, null);
            
            // Add sequence number
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            String label = String.format("%d/%d", i + 1, qrCodes.size());
            g.drawString(label, x + qrSize / 2 - 20, y + qrSize + 15);
        }
        
        g.dispose();
        return composite;
    }
}