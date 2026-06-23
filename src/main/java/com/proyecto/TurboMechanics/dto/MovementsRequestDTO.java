package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.enums.MovementType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovementsRequestDTO {

    /**tipo de mivimiento */
    @NotNull(message = "El tipo de movimiento es obligatorio")
    private MovementType type; // ENTRADA | SALIDA
    
    /** cantidad del movimiento */
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer stock;
    
    /**motivo del movimiento (opcional; si se informa, debe ser texto, no solo números) */
    @Size(max = 200)
    @Pattern(regexp = "^$|^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El motivo debe ser un texto, no solo números o símbolos")
    private String motive;
}