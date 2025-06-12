package com.textqr.converter;

import com.textqr.converter.core.QRGenerator;
import com.textqr.converter.core.TextInputHandler;
import com.textqr.converter.model.QRChunk;
import com.textqr.converter.model.QRSession;
import com.textqr.converter.ui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TextQRConverterApp extends Application {
    private static final Logger logger = LoggerFactory.getLogger(TextQRConverterApp.class);
    
    private QRGenerator qrGenerator;
    private TextInputHandler textInputHandler;
    
    @Override
    public void init() {
        logger.info("Initializing Text QR Converter Application");
        qrGenerator = new QRGenerator();
        textInputHandler = new TextInputHandler();
    }
    
    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting application UI");
        
        MainWindow mainWindow = new MainWindow(qrGenerator, textInputHandler);
        mainWindow.show(primaryStage);
    }
    
    @Override
    public void stop() {
        logger.info("Shutting down Text QR Converter Application");
    }
    
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("--cli")) {
            runCLI(args);
        } else {
            launch(args);
        }
    }
    
    private static void runCLI(String[] args) {
        try {
            TextQRConverterApp app = new TextQRConverterApp();
            app.init();
            
            if (args.length < 3) {
                System.out.println("Usage: java -jar text-qr-converter.jar --cli <input-file> <output-directory>");
                System.exit(1);
            }
            
            String inputFile = args[1];
            String outputDir = args[2];
            boolean useCompression = args.length > 3 && args[3].equals("--compress");
            
            app.processFile(inputFile, outputDir, useCompression);
            
        } catch (Exception e) {
            logger.error("Error in CLI mode", e);
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
    
    private void processFile(String inputFile, String outputDir, boolean useCompression) throws Exception {
        logger.info("Processing file: {} to directory: {}", inputFile, outputDir);
        
        // Read input text
        String text = textInputHandler.readFromFile(inputFile);
        TextInputHandler.InputStatistics stats = textInputHandler.analyzeInput(text);
        logger.info("Input statistics: {}", stats);
        
        // Create output directory
        File outDir = new File(outputDir);
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        
        // Generate QR codes
        QRSession session = qrGenerator.createSession(text, useCompression);
        logger.info("Created session {} with {} chunks", session.getSessionId(), session.getChunkCount());
        
        // Generate individual QR codes
        List<BufferedImage> qrImages = new ArrayList<>();
        for (QRChunk chunk : session.getChunks()) {
            BufferedImage qrCode = qrGenerator.generateQRCode(chunk);
            qrImages.add(qrCode);
            
            // Save individual QR code
            String filename = String.format("qr_%s_%03d.png", 
                session.getSessionId().substring(0, 8), 
                chunk.getSequenceNumber());
            File outputFile = new File(outDir, filename);
            qrGenerator.saveQRCode(qrCode, outputFile);
        }
        
        // Create composite image if multiple QR codes
        if (qrImages.size() > 1) {
            int columns = Math.min(4, qrImages.size());
            BufferedImage composite = qrGenerator.createCompositeImage(qrImages, columns);
            
            File compositeFile = new File(outDir, "qr_composite_" + session.getSessionId().substring(0, 8) + ".png");
            ImageIO.write(composite, "PNG", compositeFile);
            logger.info("Composite image saved to: {}", compositeFile.getAbsolutePath());
        }
        
        // Save session metadata
        File metadataFile = new File(outDir, "session_" + session.getSessionId().substring(0, 8) + ".txt");
        String metadata = String.format(
            "Session ID: %s\nTotal Chunks: %d\nOriginal Size: %d bytes\nChecksum: %s\nCompression: %s",
            session.getSessionId(),
            session.getChunkCount(),
            session.getTotalSize(),
            session.getFullChecksum(),
            useCompression ? "Enabled" : "Disabled"
        );
        textInputHandler.saveToFile(metadata, metadataFile.getAbsolutePath());
        
        logger.info("Processing complete. Output saved to: {}", outputDir);
    }
}