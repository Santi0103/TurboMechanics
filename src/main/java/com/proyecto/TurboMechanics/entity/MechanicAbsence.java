package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.proyecto.TurboMechanics.enums.AbsenceType;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ausencias_mecanicos")
public class MechanicAbsence {

    /** Id de la ausencia */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ausencia")
    private Long id;

    /** Mecánico al que pertenece la ausencia */
    @NotNull(message = "El mecánico es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mecanico", nullable = false)
    private Mechanic mechanic;

    /** Fecha y hora de inicio de la ausencia */
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime startDate;

    /** Fecha y hora de fin de la ausencia */
    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime endDate;

    /** Motivo de la ausencia */
    @NotBlank(message = "El motivo es obligatorio")
    @Column(name = "motivo", nullable = false, columnDefinition = "TEXT")
    private String reason;

    /** Tipo de ausencia */
    @NotNull(message = "El tipo de ausencia es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_ausencia", nullable = false, length = 30)
    private AbsenceType absenceType;

    /** Usuario que registró la ausencia */
    @Column(name = "registrado_por", length = 100)
    private String registeredBy;

    /** Fecha en que se registró la ausencia */
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) registeredAt = LocalDateTime.now();
    }
}