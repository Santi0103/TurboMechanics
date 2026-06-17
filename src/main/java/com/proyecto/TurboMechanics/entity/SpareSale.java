package com.proyecto.TurboMechanics.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.enums.SpareSaleStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "spare_sale")
public class SpareSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Spare part purchased */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SpareParts sparePart;

    /** Buyer email */
    @Column(name = "payer_email", nullable = false)
    private String payerEmail;

    /** Price at the time of purchase */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /** MercadoPago external reference */
    @Column(name = "external_reference", nullable = false)
    private String externalReference;

    /** MercadoPago preference ID */
    @Column(name = "preference_id")
    private String preferenceId;

    /** Purchase date */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** Sale status */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SpareSaleStatus status;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = SpareSaleStatus.PENDING;
    }
}