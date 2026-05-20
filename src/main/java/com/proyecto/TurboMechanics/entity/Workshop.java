package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "talleres")
public class Workshop {

    /** Id del taller */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_taller")
    private Long id;

    /** Nombre del taller */
    @NotBlank(message = "El nombre del taller es obligatorio")
    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    /** Dirección del taller */
    @NotBlank(message = "La dirección es obligatoria")
    @Column(name = "direccion", nullable = false, length = 255)
    private String address;

    /** Ciudad o municipio donde está ubicado */
    @NotBlank(message = "La ciudad es obligatoria")
    @Column(name = "ciudad", nullable = false, length = 100)
    private String city;

    /** Departamento o estado */
    @Column(name = "departamento", length = 100)
    private String state;

    /** Teléfono de contacto del taller */
    @Column(name = "telefono", length = 20)
    private String phone;

    /** Correo de contacto del taller */
    @Column(name = "correo", length = 150)
    private String email;

    /** Latitud geográfica del taller */
    @NotNull(message = "La latitud es obligatoria")
    @Column(name = "latitud", nullable = false)
    private Double latitude;

    /** Longitud geográfica del taller */
    @NotNull(message = "La longitud es obligatoria")
    @Column(name = "longitud", nullable = false)
    private Double longitude;

    /** Horario de atención del taller */
    @Column(name = "horario", length = 255)
    private String schedule;

    /** Indica si el taller está activo (solo se muestran los activos en el mapa) */
    @Column(name = "activo", nullable = false)
    private Boolean active = true;

    /** Fecha de creación del registro */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (active == null)    active    = true;
    }
}