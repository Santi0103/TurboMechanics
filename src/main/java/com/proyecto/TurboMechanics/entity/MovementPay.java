package com.proyecto.TurboMechanics.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.enums.MovementConcept;
import com.proyecto.TurboMechanics.enums.MovementType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movimiento_pago")
public class MovementPay {

    /** id del movimiento de pago */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** tipo de movimiento */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;

    /** concepto de movimiento */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementConcept concept;

    /** descripción del movimiento */
    private String description;

    /** monto del movimiento */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** factura asociada */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "factura_id")
    private Bill bill;

    /** usuario que registró */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Users registeredBy;

    /** fecha del movimiento */
    @Column(nullable = false)
    private LocalDateTime date;
}