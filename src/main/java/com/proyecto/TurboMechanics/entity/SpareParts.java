package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "repuestos")
public class SpareParts {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String reference;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false)
    private Integer stockMin;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 60)
    private String category;

    /** URL de la imagen del repuesto (subida por el admin) */
    @Column(length = 500)
    private String imageUrl;

    @OneToMany(mappedBy = "spareParts", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryMovements> movements;

    @PrePersist
    public void prePersist() {
        if (this.stockMin == null) {
            this.stockMin = 5;
        }
    }
}