package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.enums.UrgencyLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DiagnosisRequestDTO {

    /**
     * ID de la orden de trabajo a la que pertenece el diagnóstico (criterio 1).
     * Obligatorio.
     */
    @NotNull(message = "El ID de la orden de trabajo es obligatorio")
    private Long workOrderId;

    /**
     * Fallas detectadas durante la revisión técnica (criterio 2).
     * Obligatorio, debe ser texto descriptivo.
     */
    @NotBlank(message = "Las fallas detectadas son obligatorias")
    @Size(min = 5, max = 1000, message = "Las fallas detectadas deben tener entre 5 y 1000 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "Las fallas detectadas deben ser un texto descriptivo, no solo números o símbolos")
    private String detectedfailures;

    /**
     * Observaciones detalladas del mecánico (criterio 3).
     * Obligatorio, debe ser texto descriptivo.
     */
    @NotBlank(message = "Las observaciones del mecánico son obligatorias")
    @Size(min = 5, max = 1000, message = "Las observaciones deben tener entre 5 y 1000 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "Las observaciones deben ser un texto descriptivo, no solo números o símbolos")
    private String mechanicobservations;

    /**
     * Nivel de urgencia del servicio (criterio 4).
     * Valores válidos: BAJO, MEDIO, ALTO, CRITICO.
     * Obligatorio.
     */
    @NotNull(message = "El nivel de urgencia es obligatorio")
    private UrgencyLevel urgencylevel;

    /** Usuario que registra el diagnóstico */
    private String registeredby;
}