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

    /** Spare part purchased (puede quedar en null si el repuesto fue eliminado luego) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spare_part_id", nullable = true)
    private SpareParts sparePart;

    /** Copia del nombre del repuesto, usada cuando el repuesto ya fue eliminado */
    @Column(name = "spare_part_name_snapshot", length = 100)
    private String sparePartNameSnapshot;

    /** Copia de la referencia del repuesto, usada cuando el repuesto ya fue eliminado */
    @Column(name = "spare_part_reference_snapshot", length = 50)
    private String sparePartReferenceSnapshot;

    /** Copia de la categoría del repuesto, usada cuando el repuesto ya fue eliminado */
    @Column(name = "spare_part_category_snapshot", length = 60)
    private String sparePartCategorySnapshot;

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