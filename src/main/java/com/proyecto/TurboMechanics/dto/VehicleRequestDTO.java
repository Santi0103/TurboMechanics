package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VehicleRequestDTO {
    
    /** Placa del vehículo */
    @NotBlank(message = "La placa es obligatoria")
    @Size(max = 10)
    private String plate;

    /** Marca del vehículo (debe ser texto, no solo números) */
    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 50)
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "La marca no puede ser solo números o símbolos")
    private String brand;

    /** Modelo del vehículo (debe contener letras; algunos modelos llevan números) */
    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 50)
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El modelo no puede ser solo números o símbolos")
    private String model;

    /** Año del vehículo */
    @NotNull(message = "El año es obligatorio")
    @Min(value = 1900, message = "Año inválido")
    @Max(value = 2100, message = "Año inválido")
    private Integer year;

    /** Color del vehículo */
    @Size(max = 30)
    private String color;

    /**
     * ID del usuario propietario o responsable del vehículo.
     */
    @NotNull(message = "El ID del propietario es obligatorio")
    private Long ownerId;
}