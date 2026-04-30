package com.proyecto.TurboMechanics.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VehicleResponseDTO {
    
    /** ID del vehículo */
    private Long id;

    /** Placa del vehículo */
    private String plate;

    /** Marca del vehículo */
    private String brand;

    /** Modelo del vehículo */
    private String model;

    /** Año del vehículo */
    private Integer year;

    /** Color del vehículo */
    private String color;

    /** ID del propietario */
    private Long ownerId;

    /** Nombre del propietario */
    private String ownerName;

    /** Identificación (cédula) del propietario */
    private Integer ownerIdentification;

    /** Teléfono del propietario */
    private String ownerPhone;

    /** Correo del propietario */
    private String ownerEmail;

    /** Fecha en que se registró o actualizó la asociación */
    private LocalDateTime associationDate;
}
