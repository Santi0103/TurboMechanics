package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.enums.MovementType;
 
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "movimientos_inventario")
public class InventoryMovements {

    /**id del movimiento del inventario */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**Repuesto */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repuesto_id", nullable = false)
    private SpareParts spareParts;
    
    /** entrada y salida*/
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type;
    
    /**stock del inventario */
    @Column(nullable = false)
    private Integer stock;
    
    /**fecha del movimiento */
    @Column(nullable = false)
    private LocalDateTime date;
    
    /**motivo del movimiento */
    @Column(length = 200)
    private String motive;
 
    @PrePersist
    public void prePersist() {
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
    }
}
 
