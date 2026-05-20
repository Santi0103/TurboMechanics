package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.proyecto.TurboMechanics.enums.EvidenceType;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "evidencias_trabajo")
public class WorkEvidence {

    /** Id de la evidencia */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evidencia")
    private Long id;

    /** Orden de trabajo a la que pertenece la evidencia */
    @NotNull(message = "La orden de trabajo es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private WorkOrder workOrder;

    /** Nombre original del archivo subido */
    @NotBlank(message = "El nombre del archivo es obligatorio")
    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String fileName;

    /** Tipo de archivo (imagen, video, documento) */
    @NotNull(message = "El tipo de evidencia es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evidencia", nullable = false, length = 20)
    private EvidenceType evidenceType;

    /** Tipo MIME del archivo (image/jpeg, video/mp4, application/pdf, etc.) */
    @NotBlank(message = "El tipo MIME es obligatorio")
    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String mimeType;

    /** Ruta o URL donde está almacenado el archivo */
    @NotBlank(message = "La ruta del archivo es obligatoria")
    @Column(name = "ruta_archivo", nullable = false, columnDefinition = "TEXT")
    private String filePath;

    /** Tamaño del archivo en bytes */
    @Column(name = "tamano_bytes")
    private Long fileSizeBytes;

    /** Descripción o comentario sobre la evidencia */
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String description;

    /** Usuario que adjuntó la evidencia */
    @Column(name = "subido_por", length = 100)
    private String uploadedBy;

    /** Fecha en que se adjuntó la evidencia */
    @Column(name = "fecha_carga", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) uploadedAt = LocalDateTime.now();
    }
}