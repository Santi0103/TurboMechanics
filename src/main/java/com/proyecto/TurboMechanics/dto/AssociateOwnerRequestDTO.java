package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssociateOwnerRequestDTO {
    
    /**
     * ID del nuevo propietario o responsable del vehículo.
     */
    @NotNull(message = "El ID del nuevo propietario es obligatorio")
    private Long ownerId;
}
