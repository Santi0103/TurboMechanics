package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "resenas")
public class Review {

    /** ID de la reseña */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_resena")
    private Long id;

    /** Usuario que escribió la reseña */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Users user;

    /** Orden de trabajo asociada (debe estar ENTREGADO) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo_id", nullable = false)
    private WorkOrder workOrder;

    /** Comentario escrito por el cliente */
    @Column(name = "comentario", nullable = false, columnDefinition = "TEXT")
    private String comment;

    /** Calificación de 1 a 5 estrellas */
    @Column(name = "calificacion", nullable = false)
    private Integer rating;

    /** Fecha y hora en que se registró la reseña */
    @Column(name = "fecha_resena", nullable = false, updatable = false)
    private LocalDateTime reviewDate;

    /** Indica si la reseña está activa (false = eliminada por moderación o por el cliente) */
    @Column(name = "activa", nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        if (reviewDate == null) reviewDate = LocalDateTime.now();
        if (active == null)     active     = true;
    }
}