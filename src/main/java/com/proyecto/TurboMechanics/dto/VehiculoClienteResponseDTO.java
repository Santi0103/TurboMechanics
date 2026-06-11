package com.proyecto.TurboMechanics.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VehiculoClienteResponseDTO {

    /**id del vehiculo */
    private Long id;

    /**id del usuario dueño del vehiculo */
    private Long usuarioId;

    /**nombre del usuario dueño del vehiculo */
    private String nombreUsuario;

    /**placa del vehiculo */
    private String placa;

    /**marca del vehiculo */
    private String marca;

    /**modelo del vehiculo */
    private String modelo;

    /**anio del vehiculo */
    private Integer anio;

    /**color del vehiculo */
    private String color;

    /**tipo de vehiculo */
    private String tipo;

    /**cilindraje del motor */
    private String cilindraje;

    /**fecha en que el cliente registro el vehiculo */
    private LocalDateTime fechaRegistro;
}