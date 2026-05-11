package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.entity.Diagnosis;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
     * Obligatorio.
     */
    @NotBlank(message = "Las fallas detectadas son obligatorias")
    private String detectedfailures;

    /**
     * Observaciones detalladas del mecánico (criterio 3).
     * Obligatorio.
     */
    @NotBlank(message = "Las observaciones del mecánico son obligatorias")
    private String mechanicobservations;

    /**
     * Nivel de urgencia del servicio (criterio 4).
     * Valores válidos: BAJO, MEDIO, ALTO, CRITICO.
     * Obligatorio.
     */
    @NotNull(message = "El nivel de urgencia es obligatorio")
    private Diagnosis.UrgencyLevel urgencylevel;

    /** Usuario que registra el diagnóstico */
    private String registeredby;
}
