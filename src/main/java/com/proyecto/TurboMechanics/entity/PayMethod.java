package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "metodo_pago")
public class PayMethod {

    /**id del metodo de pago */
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**nombre del metodo de pago */
    @Column(nullable = false, unique = true)
    private String name; 

    /** descripcion del metodo de pago */
    @Column(nullable = false, name = "descripcion")
    private String description;
    
    /**activo o inactivo del metodo de pago */
    @Column(nullable = false)
    private boolean active;

    /**parametros del metodo de pago */
    @Column(name = "config_json")
    private String configJson;
}
