package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class WorkshopRequestDTO {

    /** Nombre del taller (obligatorio, debe ser texto) */
    @NotBlank(message = "El nombre del taller es obligatorio")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El nombre del taller no puede ser solo números o símbolos")
    private String name;

    /** Dirección (obligatoria) */
    @NotBlank(message = "La dirección es obligatoria")
    private String address;

    /** Ciudad (obligatoria, debe ser texto) */
    @NotBlank(message = "La ciudad es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "La ciudad no puede ser solo números o símbolos")
    private String city;

    /** Departamento (opcional) */
    private String state;

    /** Teléfono de contacto (opcional; si se informa, debe ser un teléfono válido) */
    @Pattern(regexp = "^$|^[+]?[0-9\\s\\-]{7,20}$", message = "Formato de teléfono inválido")
    private String phone;

    /** Correo de contacto (opcional; si se informa, debe ser un correo válido) */
    @Pattern(regexp = "^$|^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$", message = "Correo inválido")
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