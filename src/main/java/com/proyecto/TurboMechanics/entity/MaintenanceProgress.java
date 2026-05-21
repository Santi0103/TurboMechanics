package com.proyecto.TurboMechanics.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "progreso_mantenimiento")
public class MaintenanceProgress {

    /** id del mantenimiento */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Orden de trabajo asociada */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false)
    private WorkOrder workOrder;

    /** Descripción del avance registrado por el mecánico */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    /** Fecha y hora en que se registró el avance */
    @Column(nullable = false)
    private LocalDateTime registeredAt;

    /** Mecánico que registró el avance */
    @Column(name = "registered_by", length = 100)
    private String registeredBy;

    @PrePersist
    protected void onCreate() {
        if (registeredAt == null) registeredAt = LocalDateTime.now();
    }
}