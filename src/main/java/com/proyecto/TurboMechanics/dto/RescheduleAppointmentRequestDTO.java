package com.proyecto.TurboMechanics.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RescheduleAppointmentRequestDTO {

    /** Nueva fecha de la cita */
    @NotNull(message = "La nueva fecha es obligatoria")
    @Future(message = "La fecha debe ser en el futuro")
    private LocalDate newDate;

    /** Nueva hora de la cita */
    @NotNull(message = "La nueva hora es obligatoria")
    private LocalTime newTime;
}