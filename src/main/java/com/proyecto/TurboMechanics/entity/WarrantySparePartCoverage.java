package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Representa UN repuesto cubierto dentro de una garantía. Una garantía puede
 * tener varias filas de este tipo (una por cada repuesto seleccionado al
 * registrarla), a diferencia de los servicios (que se relacionan directo
 * por una tabla intermedia simple, ya que no necesitan snapshot).
 * <p>
 * Se modela como entidad propia (y no como ManyToMany directo) porque, igual
 * que en SpareSale, necesitamos guardar una copia (snapshot) del nombre,
 * referencia y categoría del repuesto para el caso en que el repuesto sea
 * eliminado del inventario más adelante: la garantía y su comprobante deben
 * seguir mostrando qué repuesto cubrían.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "garantia_repuestos")
public class WarrantySparePartCoverage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Garantía a la que pertenece este repuesto cubierto */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_garantia", nullable = false)
    private Warranty warranty;

    /** Repuesto cubierto (puede quedar en null si luego se elimina del inventario) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_repuesto")
    private SpareParts sparePart;

    /** Copia del nombre del repuesto, usada cuando el repuesto ya fue eliminado */
    @Column(name = "nombre_snapshot", length = 100)
    private String nameSnapshot;

    /** Copia de la referencia del repuesto, usada cuando el repuesto ya fue eliminado */
    @Column(name = "referencia_snapshot", length = 50)
    private String referenceSnapshot;

    /** Copia de la categoría del repuesto, usada cuando el repuesto ya fue eliminado */
    @Column(name = "categoria_snapshot", length = 60)
    private String categorySnapshot;
}