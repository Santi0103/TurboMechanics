package com.proyecto.TurboMechanics.dto;

import lombok.Data;

@Data
public class WorkshopResponseDTO {

    /** Id del taller */
    private Long id;

    /** Nombre del taller */
    private String name;

    /** Dirección */
    private String address;

    /** Ciudad */
    private String city;

    /** Departamento */
    private String state;

    /** Teléfono de contacto */
    private String phone;

    /** Correo de contacto */
    private String email;

    /** Latitud para el mapa */
    private Double latitude;

    /** Longitud para el mapa */
    private Double longitude;

    /** Horario de atención */
    private String schedule;

    /** Indica si el taller está activo */
    private Boolean active;

    /**
     * Distancia aproximada en kilómetros desde la ubicación del cliente.
     * Calculada en el servicio cuando el cliente envía su posición.
     */
    private Double distanceKm;
}