package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "items_control_calidad")
public class QualityCheckItem {

    /** Id del ítem */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_item")
    private Long id;

    /** Control de calidad al que pertenece este ítem */
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_control", nullable = false)
    private QualityCheck qualityCheck;

    /** Nombre del servicio que se debe verificar */
    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Column(name = "servicio", nullable = false, length = 150)
    private String serviceName;

    /** Id del servicio en la tabla servicios (referencia informativa) */
    @Column(name = "id_servicio")
    private Long serviceId;

    /** Indica si el servicio fue verificado satisfactoriamente */
    @Column(name = "verificado", nullable = false)
    private Boolean verified = false;

    /** Observación específica del ítem */
    @Column(name = "observacion", columnDefinition = "TEXT")
    private String observation;

    /** Usuario que marcó el ítem como verificado */
    @Column(name = "verificado_por", length = 100)
    private String verifiedBy;

    /** Fecha en que se marcó como verificado */
    @Column(name = "fecha_verificacion")
    private LocalDateTime verifiedAt;

    @PrePersist
    protected void onCreate() {
        if (verified == null) verified = false;
    }
}