package com.textqr.converter.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class QRSession {
    private final String sessionId;
    private final String originalText;
    private final List<QRChunk> chunks;
    private final LocalDateTime createdAt;
    private final int totalSize;
    private final String fullChecksum;

    public QRSession(String originalText, String fullChecksum) {
        this.sessionId = UUID.randomUUID().toString();
        this.originalText = originalText;
        this.fullChecksum = fullChecksum;
        this.chunks = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.totalSize = originalText.getBytes().length;
    }

    public void addChunk(QRChunk chunk) {
        chunks.add(chunk);
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getOriginalText() {
        return originalText;
    }

    public List<QRChunk> getChunks() {
        return new ArrayList<>(chunks);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getTotalSize() {
        return totalSize;
    }

    public String getFullChecksum() {
        return fullChecksum;
    }

    public int getChunkCount() {
        return chunks.size();
    }
}