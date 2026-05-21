package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WorkshopRequestDTO {

    /** Nombre del taller (obligatorio) */
    @NotBlank(message = "El nombre del taller es obligatorio")
    private String name;

    /** Dirección (obligatoria) */
    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    /** Ciudad (obligatoria) */
    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    /** Departamento (opcional) */
    private String state;

    /** Teléfono de contacto (opcional) */
    private String phone;

    /** Correo de contacto (opcional) */
    private String email;

    /** Latitud geográfica (obligatoria) */
    @NotNull(message = "La latitud es obligatoria")
    private Double latitude;

    /** Longitud geográfica (obligatoria) */
    @NotNull(message = "La longitud es obligatoria")
    private Double longitude;

    /** Horario de atención (opcional) */
    private String schedule;

    /** Estado activo/inactivo (opcional, por defecto true) */
    private Boolean active;
}