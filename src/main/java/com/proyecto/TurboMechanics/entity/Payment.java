package com.proyecto.TurboMechanics.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.enums.PaymentStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Factura a la que corresponde este pago */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false)
    private Bill bill;

    /** Id de pago devuelto por MercadoPago */
    @Column(name = "mp_payment_id", unique = true)
    private Long mpPaymentId;

    /**
     * Referencia externa que enviamos a MercadoPago.
     * Formato: numBill-timestamp
     */
    @Column(name = "external_reference", nullable = false)
    private String externalReference;

    /** Monto en pesos colombianos */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Método de pago usado:
     * credit_card | debit_card | pse | efecty | bank_transfer
     */
    @Column(name = "payment_method")
    private String paymentMethod;

    /** Estado del pago según MercadoPago */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    /**
     * URL donde el cliente completa el pago (Checkout Pro).
     * Presente para PSE, Efecty y otros métodos offline.
     */
    @Column(name = "init_point", columnDefinition = "TEXT")
    private String initPoint;

    /** Id de preferencia de MercadoPago (Checkout Pro) */
    @Column(name = "mp_preference_id")
    private String mpPreferenceId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null)    status    = PaymentStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}