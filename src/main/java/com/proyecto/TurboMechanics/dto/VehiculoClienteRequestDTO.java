package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class VehiculoClienteRequestDTO {

    /**placa del vehiculo */
    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Za-z0-9\\-]{3,10}$", message = "Placa inválida")
    private String placa;

    /**marca del vehiculo */
    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    /**modelo del vehiculo */
    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    /**anio del vehiculo */
    @NotNull(message = "El año es obligatorio")
    @Min(value = 1970, message = "Año mínimo 1970")
    @Max(value = 2030, message = "Año máximo 2030")
    private Integer anio;

    /**color del vehiculo */
    private String color;

    /**tipo de vehiculo (carro, moto, camioneta, etc) */
    private String tipo;

    /**cilindraje del motor */
    private String cilindraje;
}