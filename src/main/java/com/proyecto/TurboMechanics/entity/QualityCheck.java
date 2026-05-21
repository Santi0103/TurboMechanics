package com.proyecto.TurboMechanics.entity;

import com.proyecto.TurboMechanics.enums.QualityCheckStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "controles_calidad")
public class QualityCheck {

    /** Id del control de calidad */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_control")
    private Long id;

    /** Orden de trabajo asociada al control de calidad */
    @NotNull(message = "La orden de trabajo es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private WorkOrder workOrder;

    /** Estado actual del control de calidad */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private QualityCheckStatus status = QualityCheckStatus.EN_PROCESO;

    /** Observaciones generales del control de calidad */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observations;

    /** Lista de ítems de verificación (uno por servicio de la orden) */
    @OneToMany(mappedBy = "qualityCheck", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<QualityCheckItem> items = new ArrayList<>();

    /** Usuario que inició el control de calidad */
    @Column(name = "creado_por", length = 100)
    private String createdBy;

    /** Fecha de inicio del control */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Usuario que aprobó o rechazó el control */
    @Column(name = "aprobado_por", length = 100)
    private String approvedBy;

    /** Fecha en que se aprobó o rechazó el control */
    @Column(name = "fecha_aprobacion")
    private LocalDateTime approvedAt;

    /** Fecha de la última actualización */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime updatedAt;

    /** Usuario que realizó la última actualización */
    @Column(name = "actualizado_por", length = 100)
    private String updatedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null)    status    = QualityCheckStatus.EN_PROCESO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}