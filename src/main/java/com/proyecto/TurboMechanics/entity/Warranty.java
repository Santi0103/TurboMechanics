package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.proyecto.TurboMechanics.enums.WarrantyStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "garantias")
public class Warranty {

    /** Id de la garantía */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_garantia")
    private Long id;

    /** Orden de trabajo a la que pertenece la garantía */
    @NotNull(message = "La orden de trabajo es obligatoria")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden_trabajo", nullable = false)
    private WorkOrder workOrder;

    /** Servicios cubiertos por la garantía (puede tener varios, o ninguno si solo cubre repuestos).
     *  Se modela como entidad hija (no ManyToMany directo) para poder guardar un snapshot del
     *  nombre, igual que con los repuestos: si el servicio se elimina del catálogo después de
     *  crear la garantía, el nombre se sigue mostrando. */
    @OneToMany(mappedBy = "warranty", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WarrantyServiceCoverage> serviceCoverages = new ArrayList<>();

    /** Repuestos cubiertos por la garantía (puede tener varios, o ninguno si solo cubre servicios) */
    @OneToMany(mappedBy = "warranty", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<WarrantySparePartCoverage> sparePartCoverages = new ArrayList<>();

    /** Fecha de inicio de la vigencia de la garantía */
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    @Column(name = "fecha_inicio_vigencia", nullable = false)
    private LocalDate startDate;

    /** Fecha de fin de la vigencia de la garantía */
    @NotNull(message = "La fecha de fin de vigencia es obligatoria")
    @Column(name = "fecha_fin_vigencia", nullable = false)
    private LocalDate endDate;

    /** Estado actual de la garantía */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private WarrantyStatus status = WarrantyStatus.ACTIVA;

    /** Observaciones o condiciones de la garantía */
    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observations;

    /** Motivo del cierre de la garantía */
    @Column(name = "motivo_cierre", columnDefinition = "TEXT")
    private String closureReason;

    /** Fecha en que se cerró la garantía */
    @Column(name = "fecha_cierre")
    private LocalDateTime closureDate;

    /** Usuario que realizó el cierre */
    @Column(name = "cerrado_por", length = 100)
    private String closedBy;

    /** Número de comprobante de garantía generado */
    @Column(name = "numero_comprobante", unique = true, length = 30)
    private String voucherNumber;

    /** Fecha en que se generó el comprobante */
    @Column(name = "fecha_generacion_comprobante")
    private LocalDateTime voucherGeneratedAt;

    /** Usuario que generó el comprobante */
    @Column(name = "comprobante_generado_por", length = 100)
    private String voucherGeneratedBy;

    /** Usuario que registró la garantía */
    @Column(name = "creado_por", length = 100)
    private String createdBy;

    /** Fecha de creación del registro */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Usuario que realizó la última modificación */
    @Column(name = "actualizado_por", length = 100)
    private String updatedBy;

    /** Fecha de la última modificación */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null)    status    = WarrantyStatus.ACTIVA;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}