package com.textqr.converter.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TextInputHandler {
    private static final Logger logger = LoggerFactory.getLogger(TextInputHandler.class);
    
    public enum InputType {
        FILE,
        CLIPBOARD,
        DIRECT
    }
    
    public String readFromFile(String filePath) throws IOException {
        logger.info("Reading text from file: {}", filePath);
        
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        
        if (!Files.isReadable(path)) {
            throw new IOException("File is not readable: " + filePath);
        }
        
        byte[] content = Files.readAllBytes(path);
        String text = new String(content, StandardCharsets.UTF_8);
        
        logger.info("Successfully read {} bytes from file", content.length);
        return text;
    }
    
    public String readFromFile(File file) throws IOException {
        return readFromFile(file.getAbsolutePath());
    }
    
    public String readFromClipboard() throws IOException, UnsupportedFlavorException {
        logger.info("Reading text from clipboard");
        
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        
        if (!clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            throw new UnsupportedFlavorException(DataFlavor.stringFlavor);
        }
        
        String text = (String) clipboard.getData(DataFlavor.stringFlavor);
        
        if (text == null || text.isEmpty()) {
            throw new IOException("Clipboard is empty");
        }
        
        logger.info("Successfully read {} characters from clipboard", text.length());
        return text;
    }
    
    public void copyToClipboard(String text) {
        logger.info("Copying {} characters to clipboard", text.length());
        
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
        
        logger.info("Text copied to clipboard successfully");
    }
    
    public void saveToFile(String text, String filePath) throws IOException {
        logger.info("Saving text to file: {}", filePath);
        
        Path path = Paths.get(filePath);
        
        // Create parent directories if they don't exist
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }
        
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        
        logger.info("Successfully saved {} bytes to file", text.length());
    }
    
    public String validateAndNormalizeText(String text) {
        if (text == null) {
            return "";
        }
        
        // Normalize line endings
        text = text.replaceAll("\r\n", "\n").replaceAll("\r", "\n");
        
        // Remove any null characters
        text = text.replace("\0", "");
        
        // Trim trailing whitespace
        text = text.trim();
        
        return text;
    }
    
    public InputStatistics analyzeInput(String text) {
        return new InputStatistics(text);
    }
    
    public static class InputStatistics {
        private final int totalCharacters;
        private final int totalLines;
        private final int totalBytes;
        private final boolean hasNonAscii;
        
        public InputStatistics(String text) {
            this.totalCharacters = text.length();
            this.totalLines = text.split("\n").length;
            this.totalBytes = text.getBytes(StandardCharsets.UTF_8).length;
            this.hasNonAscii = !text.matches("\\p{ASCII}*");
        }
        
        public int getTotalCharacters() {
            return totalCharacters;
        }
        
        public int getTotalLines() {
            return totalLines;
        }
        
        public int getTotalBytes() {
            return totalBytes;
        }
        
        public boolean hasNonAscii() {
            return hasNonAscii;
        }
        
        @Override
        public String toString() {
            return String.format(
                "Characters: %d, Lines: %d, Bytes: %d, Non-ASCII: %s",
                totalCharacters, totalLines, totalBytes, hasNonAscii ? "Yes" : "No"
            );
        }
    }
}