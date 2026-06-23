package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    /** Motivo de la ausencia (debe ser texto explicativo, no solo números) */
    @NotBlank(message = "El motivo es obligatorio")
    @Size(min = 3, max = 300, message = "El motivo debe tener entre 3 y 300 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El motivo debe ser un texto explicativo, no solo números o símbolos")
    private String reason;

    /** Tipo de ausencia */
    @NotNull(message = "El tipo de ausencia es obligatorio")
    private AbsenceType absenceType;
}