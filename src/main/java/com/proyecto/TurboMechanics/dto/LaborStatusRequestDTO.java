package com.proyecto.TurboMechanics.dto;
import com.proyecto.TurboMechanics.enums.LaborStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LaborStatusRequestDTO {

    /** Nuevo estado laboral del mecánico (ACTIVO, INACTIVO, SUSPENDIDO, RETIRADO) */
    @NotNull(message = "El estado laboral es obligatorio")
    private LaborStatus laborStatus;
}