package com.proyecto.TurboMechanics.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.enums.StatusEstimate;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "presupuesto")
public class Estimate {

    /** id del presupuesto */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** id de la orden de trabajo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_trabajo_id", nullable = false)
    private WorkOrder workOrder;

    /** usuario asociado */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Users users;

    /** placa del vehiculo */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_placa", referencedColumnName = "placa", nullable = false)
    private Vehicle vehicle;

    /**
     * JSON con el detalle: repuestos, mano de obra,
     * tiempos estimados y costos parciales
     */
    @Column(name = "detalle_json", columnDefinition = "TEXT")
    private String detailJson;

    /** total estimado del presupuesto */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalEstimate;

    /** estado presupuesto */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusEstimate statusEstimate;

    /** fecha de envio del presupuesto */
    private LocalDateTime dateSent;

    /** fecha de la respuesta del presupuesto */
    private LocalDateTime dateResponse;
}