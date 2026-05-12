package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparePartsRequestDTO {

    /**nombre del repuesto */
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String name;

    /**referencia del repuesto */
    @NotBlank(message = "La referencia es obligatoria")
    @Size(max = 50)
    private String reference;
    
    /** cantidad del repuesto */
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Integer stock;
    
    /**precio del repuesto */
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;
    
    /**categoria del repuesto */
    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 60)
    private String category;
    
    /**cantidad minima */
    @Min(value = 0)
    private Integer stockMin;
}
