package com.textqr.converter.model;

import java.time.LocalDateTime;

public class QRChunk {
    private final int sequenceNumber;
    private final int totalChunks;
    private final String data;
    private final String checksum;
    private final LocalDateTime timestamp;
    private final String sessionId;

    public QRChunk(int sequenceNumber, int totalChunks, String data, String checksum, String sessionId) {
        this.sequenceNumber = sequenceNumber;
        this.totalChunks = totalChunks;
        this.data = data;
        this.checksum = checksum;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public String getData() {
        return data;
    }

    public String getChecksum() {
        return checksum;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String toJsonString() {
        return String.format("{\"seq\":%d,\"total\":%d,\"data\":\"%s\",\"checksum\":\"%s\",\"session\":\"%s\"}",
                sequenceNumber, totalChunks, data, checksum, sessionId);
    }
}