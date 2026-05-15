package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.proyecto.TurboMechanics.enums.AbsenceType;
import java.time.LocalDateTime;

@Data
public class MechanicAbsenceRequestDTO {

    /** Fecha y hora de inicio de la ausencia */
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime startDate;

    /** Fecha y hora de fin de la ausencia */
    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime endDate;

    /** Motivo de la ausencia */
    @NotBlank(message = "El motivo es obligatorio")
    private String reason;

    /** Tipo de ausencia */
    @NotNull(message = "El tipo de ausencia es obligatorio")
    private AbsenceType absenceType;
}