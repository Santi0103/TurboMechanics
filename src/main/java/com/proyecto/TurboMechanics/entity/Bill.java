package com.proyecto.TurboMechanics.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.proyecto.TurboMechanics.enums.StatusBill;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "factura")
public class Bill {

    /** id de la factura */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** numero de la factura */
    @Column(name = "numero_factura", unique = true, nullable = false)
    private String numBill;

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

    /** metodo de pago */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id")
    private PayMethod payMethod;

    /** fecha de la factura */
    @Column(nullable = false)
    private LocalDate date;

    /** sub total de la factura */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /** impuestos de la factura */
    @Column(precision = 12, scale = 2)
    private BigDecimal taxes;

    /** total de la factura */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    /** estado de la factura */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusBill status;

    /** nombre del mecanico o admin que creo la factura */
    @Column(name = "creada_por")
    private String createdBy;
}