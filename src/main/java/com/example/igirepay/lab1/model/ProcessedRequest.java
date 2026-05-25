package com.example.igirepay.lab1.model;

import java.time.LocalDateTime;

public class ProcessedRequest {

    private int id;
    private String referenceId;
    private LocalDateTime processedAt;

    public ProcessedRequest() {}

    public ProcessedRequest(int id, String referenceId, LocalDateTime processedAt) {
        this.id = id;
        this.referenceId = referenceId;
        this.processedAt = processedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    @Override
    public String toString() {
        return "ProcessedRequest{" +
                "id=" + id +
                ", referenceId='" + referenceId + '\'' +
                ", processedAt=" + processedAt +
                '}';
    }
}