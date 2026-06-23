package com.proyecto.TurboMechanics.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateAppointmentRequestDTO {
    /** Identificación del cliente (solo números, mínimo 5 dígitos) */
    @NotNull(message = "La identificación es obligatoria")
    @Positive(message = "La identificación debe ser válida")
    @Min(value = 10000, message = "La identificación debe tener al menos 5 dígitos")
    private Integer identification;
 
    /** Placa del vehículo */
    @NotBlank(message = "La placa es obligatoria")
    private String plate;
 
    /** Fecha de la cita (debe ser futura) */
    @NotNull(message = "La fecha es obligatoria")
    @Future(message = "La fecha debe ser en el futuro")
    private LocalDate date;
 
    /** Hora de la cita */
    @NotNull(message = "La hora es obligatoria")
    private LocalTime time;
 
    /** Motivo del servicio (opcional; si se informa, debe ser texto, no solo números) */
    @Pattern(regexp = "^$|^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El motivo debe ser un texto, no solo números o símbolos")
    private String reason;
 
    /** Usuario que registra */
    @NotBlank(message = "El usuario creador es obligatorio")
    private String createdBy;
}