package com.proyecto.TurboMechanics.dto;

import java.time.LocalTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityAppointmentResponseDTO {

    /** Horarios disponibles en la fecha consultada */
    private List<LocalTime> availableSlots;

    /** Horarios ocupados en la fecha consultada */
    private List<LocalTime> occupiedSlots;
}