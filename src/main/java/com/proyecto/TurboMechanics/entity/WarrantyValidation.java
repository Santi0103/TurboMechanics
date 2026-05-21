package com.proyecto.TurboMechanics.entity;

import com.proyecto.TurboMechanics.enums.WarrantyValidationStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "validaciones_garantia")
public class WarrantyValidation {

    /** Id de la validación */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_validacion")
    private Long id;

    /** Garantía que fue validada */
    @NotNull(message = "La garantía es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_garantia", nullable = false)
    private Warranty warranty;

    /** Resultado de la validación */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "resultado", nullable = false, length = 20)
    private WarrantyValidationStatus result;

    /** Mensaje informativo del resultado de la validación */
    @Column(name = "mensaje", nullable = false, columnDefinition = "TEXT")
    private String message;

    /** Indica si la cobertura fue aprobada en esta validación */
    @Column(name = "cobertura_aprobada", nullable = false)
    private Boolean coverageApproved = false;

    /** Motivo de rechazo cuando la cobertura no se aprueba (opcional) */
    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    private String rejectionReason;

    /** Usuario que realizó la validación */
    @Column(name = "validado_por", nullable = false, length = 100)
    private String validatedBy;

    /** Fecha y hora en que se realizó la validación */
    @Column(name = "fecha_validacion", nullable = false, updatable = false)
    private LocalDateTime validatedAt;

    @PrePersist
    protected void onCreate() {
        if (validatedAt == null) validatedAt = LocalDateTime.now();
        if (coverageApproved == null) coverageApproved = false;
    }
}