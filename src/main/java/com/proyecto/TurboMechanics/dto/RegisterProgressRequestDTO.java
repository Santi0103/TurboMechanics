package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterProgressRequestDTO {

    /** id de la orden de trabajo */
    @NotNull(message = "El id de la orden es obligatorio")
    @Positive(message = "El id debe ser mayor a 0")
    private Long workOrderId;

    /** descripcion del avance (debe ser texto explicativo) */
    @NotBlank(message = "La descripción del avance es obligatoria")
    @Size(min = 5, max = 1000, message = "La descripción debe tener entre 5 y 1000 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "La descripción debe ser un texto explicativo, no solo números o símbolos")
    private String description;

    /** mecanico que registro el progreso */
    @NotBlank(message = "El mecánico es obligatorio")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El nombre del mecánico no puede ser solo números")
    private String registeredBy;
}