package com.proyecto.TurboMechanics.dto;

import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.enums.EvidenceType;

import lombok.Data;

@Data
public class WorkEvidenceResponseDTO {

    private Long id;
    private Long workOrderId;
    private String workOrderNumber;
    private String fileName;
    private EvidenceType evidenceType;
    private String mimeType;
    private String filePath;
    private String fileUrl;        // ← URL pública para el frontend
    private Long fileSizeBytes;
    private String description;
    private String uploadedBy;
    private LocalDateTime uploadedAt;
}