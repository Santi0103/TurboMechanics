package com.proyecto.TurboMechanics.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "historial_notificaciones")
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Orden de trabajo a la que pertenece la notificación */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo_id", nullable = false)
    private WorkOrder workOrder;

    /** Canal usado: Email, Whastapp, Both */
    @Column(name = "canal", nullable = false, length = 20)
    private String canal;

    /** Asunto del mensaje */
    @Column(name = "asunto", length = 255)
    private String asunto;

    /** Cuerpo del mensaje */
    @Column(name = "cuerpo", columnDefinition = "TEXT")
    private String cuerpo;

    /** Fecha y hora del envío */
    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;

    @PrePersist
    protected void onCreate() {
        if (fechaEnvio == null) fechaEnvio = LocalDateTime.now();
    }
}