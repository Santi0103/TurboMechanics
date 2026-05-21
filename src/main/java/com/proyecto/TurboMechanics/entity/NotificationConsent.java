package com.proyecto.TurboMechanics.entity;

import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.enums.NotificationChannel;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "consentimiento_notificacion")
public class NotificationConsent {

    /**id de la notificacion */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cliente al que pertenece el consentimiento (relación por cédula) */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_cedula", referencedColumnName = "cedula", nullable = false)
    private Users users;

    /** true = el cliente autorizó el envío de notificaciones */
    @Column(nullable = false)
    private boolean authorized;

    /** Canal preferido: EMAIL, WHATSAPP o BOTH */
    @Enumerated(EnumType.STRING)
    @Column
    private NotificationChannel channel;

    /** Timestamp del primer registro del consentimiento */
    @Column(nullable = false)
    private LocalDateTime consentAt;

    /** Timestamp de la última actualización */
    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (consentAt == null) consentAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}