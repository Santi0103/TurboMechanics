package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa UN servicio cubierto dentro de una garantía. Una garantía puede
 * tener varias filas de este tipo (una por cada servicio seleccionado al
 * registrarla).
 * <p>
 * Igual que con WarrantySparePartCoverage, se modela como entidad propia (y
 * no como ManyToMany directo) para poder guardar una copia (snapshot) del
 * nombre del servicio: si el servicio es eliminado del catálogo más
 * adelante, la garantía y su comprobante deben seguir mostrando qué
 * servicio cubrían.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "garantia_servicios_cobertura")
public class WarrantyServiceCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Garantía a la que pertenece este servicio cubierto */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_garantia", nullable = false)
    private Warranty warranty;

    /** Servicio cubierto (puede quedar en null si luego se elimina del catálogo) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio")
    private ServiceEntity service;

    /** Copia del nombre del servicio, usada cuando el servicio ya fue eliminado */
    @Column(name = "nombre_snapshot", length = 100)
    private String nameSnapshot;
}