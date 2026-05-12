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
    
    /** id del repuesto */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**nombre del repuesto */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**referecia del repuesto */
    @Column(nullable = false, unique = true, length = 50)
    private String reference;
    
    /** Stock de los repuestos */
    @Column(nullable = false)
    private Integer stock;
    
    /**stock minimo de repuesto */
    @Column(nullable = false)
    private Integer stockMin;
    
    /**precio del repuesto */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    /** categoria de los repuestos */
    @Column(nullable = false, length = 60)
    private String category;
    
    /**historial de los repuestos */
    @OneToMany(mappedBy = "spareParts", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<InventoryMovements> movements;
 
    @PrePersist
    public void prePersist() {
        if (this.stockMin == null) {
            this.stockMin = 5;
        }
    }
}
