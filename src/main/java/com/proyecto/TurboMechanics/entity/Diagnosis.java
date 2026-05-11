package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "diagnosticos")
public class Diagnosis {

    /** ID del diagnóstico */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Orden de trabajo a la que está asociado el diagnóstico.
     * Relación ManyToOne: una orden puede tener múltiples diagnósticos en diferentes momentos,
     * pero el activo será el más reciente.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo_id", nullable = false)
    private WorkOrder workOrder;

    /**
     * Fallas detectadas durante la revisión técnica.
     * Campo obligatorio.
     */
    @Column(name = "fallas_detectadas", nullable = false, columnDefinition = "TEXT")
    private String detectedfailures;

    /**
     * Observaciones detalladas del mecánico.
     * Campo obligatorio.
     */
    @Column(name = "observaciones_mecanico", nullable = false, columnDefinition = "TEXT")
    private String mechanicobservations;

    /**
     * Nivel de urgencia del servicio.
     * Valores: BAJO, MEDIO, ALTO, CRITICO.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_urgencia", nullable = false, length = 20)
    private UrgencyLevel urgencylevel;

    /**
     * Indica si el diagnóstico fue usado para generar una orden de trabajo (criterio 7).
     */
    @Column(name = "orden_generada", nullable = false)
    private boolean ordergenerated = false;

    /** Mecánico o administrador que registró el diagnóstico */
    @Column(name = "registrado_por", length = 100)
    private String registeredby;

    /** Fecha y hora en que se registró el diagnóstico */
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime registrationdate;

    /** Fecha y hora de la última actualización (criterio 6) */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime updatedate;

    @PrePersist
    protected void onCreate() {
        if (registrationdate == null) registrationdate = LocalDateTime.now();
        if (urgencylevel == null)     urgencylevel     = UrgencyLevel.MEDIO;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedate = LocalDateTime.now();
    }

    /** Niveles de urgencia del diagnóstico */
    public enum UrgencyLevel { BAJO, MEDIO, ALTO, CRITICO }
}
