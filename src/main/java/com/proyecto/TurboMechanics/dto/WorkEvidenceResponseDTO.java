package com.proyecto.TurboMechanics.dto;

import lombok.Data;
import com.proyecto.TurboMechanics.enums.EvidenceType;
import java.time.LocalDateTime;

@Data
public class WorkEvidenceResponseDTO {

    /** Id de la evidencia */
    private Long id;

    /** Id de la orden de trabajo */
    private Long workOrderId;

    /** Número de orden de trabajo */
    private String workOrderNumber;

    /** Nombre original del archivo */
    private String fileName;

    /** Tipo de evidencia (IMAGEN, VIDEO, DOCUMENTO) */
    private EvidenceType evidenceType;

    /** Tipo MIME del archivo */
    private String mimeType;

    /** URL o ruta de acceso al archivo */
    private String filePath;

    /** Tamaño del archivo en bytes */
    private Long fileSizeBytes;

    /** Descripción o comentario */
    private String description;

    /** Usuario que adjuntó la evidencia */
    private String uploadedBy;

    /** Fecha de carga */
    private LocalDateTime uploadedAt;
}