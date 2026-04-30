package com.proyecto.TurboMechanics.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "vehiculos")
public class Vehicle {
    
    /** ID del vehículo */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Placa del vehículo (única) */
    @Column(name = "placa", nullable = false, unique = true, length = 10)
    private String plate;

    /** Marca del vehículo */
    @Column(name = "marca", nullable = false, length = 50)
    private String brand;

    /** Modelo del vehículo */
    @Column(name = "modelo", nullable = false, length = 50)
    private String model;

    /** Año del vehículo */
    @Column(name = "anio", nullable = false)
    private Integer year;

    /** Color del vehículo */
    @Column(name = "color", length = 30)
    private String color;

    /**
     * Cliente propietario o responsable del vehículo.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Users owner;

    /** Fecha en que se registró o actualizó la asociación */
    @Column(name = "fecha_asociacion", nullable = false)
    private LocalDateTime associationDate;

    @PrePersist
    protected void onCreate() {
        if (associationDate == null) associationDate = LocalDateTime.now();
        if (plate != null) plate = plate.toUpperCase().trim();
    }

    @PreUpdate
    protected void onUpdate() {
        associationDate = LocalDateTime.now();
    }
}
