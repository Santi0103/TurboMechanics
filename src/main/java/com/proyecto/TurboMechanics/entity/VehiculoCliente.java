package com.proyecto.TurboMechanics.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "vehiculos_cliente")
public class VehiculoCliente {

    /**id del vehiculo */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**id del usuario dueño del vehiculo (userId del JWT) */
    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    /**placa del vehiculo */
    @Column(name = "placa", nullable = false, length = 10)
    private String placa;

    /**marca del vehiculo */
    @Column(name = "marca", nullable = false, length = 50)
    private String marca;

    /**modelo del vehiculo */
    @Column(name = "modelo", nullable = false, length = 50)
    private String modelo;

    /**anio del vehiculo */
    @Column(name = "anio", nullable = false)
    private Integer anio;

    /**color del vehiculo */
    @Column(name = "color", length = 30)
    private String color;

    /**tipo de vehiculo (carro, moto, camioneta, etc) */
    @Column(name = "tipo", length = 30)
    private String tipo;

    /**cilindraje del motor */
    @Column(name = "cilindraje", length = 20)
    private String cilindraje;

    /**fecha en que el cliente registro el vehiculo */
    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) fechaRegistro = LocalDateTime.now();
        if (placa != null) placa = placa.toUpperCase().trim();
    }
}