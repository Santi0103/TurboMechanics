package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
 
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "servicios")
public class ServiceEntity {
    
    /**Id del servicio */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**nombre del servicio */
    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, length = 100)
    private String name;
    
    /** descripcion del servicio */
    @NotBlank(message = "La descripcion es obligatoria")
    @Column(name = "descripcion",nullable = false, columnDefinition = "TEXT")
    private String description;
    
    /** precio del servicio */
    @NotNull(message = "El precio es obligatorio")
    @Column(name = "precio",nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    /** Activo o inactivo del servicio */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @PrePersist
    public void prePersist() {
        if (this.active == null) {
            this.active = true;
        }
    }
}
