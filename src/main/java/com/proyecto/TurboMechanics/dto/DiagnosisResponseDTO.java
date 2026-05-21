package com.proyecto.TurboMechanics.dto;

import lombok.Data;
import com.proyecto.TurboMechanics.enums.UrgencyLevel;
import java.time.LocalDateTime;

@Data
public class DiagnosisResponseDTO {

    /** ID del diagnóstico */
    private Long id;

    /** ID de la orden de trabajo asociada */
    private Long workOrderId;

    /** Número de la orden de trabajo (para facilitar la respuesta al cliente) */
    private String workOrderNumber;

    /** Placa del vehículo diagnosticado */
    private String vehicleplate;

    /** Fallas detectadas durante la revisión técnica (criterio 2) */
    private String detectedfailures;

    /** Observaciones detalladas del mecánico (criterio 3) */
    private String mechanicobservations;

    /** Nivel de urgencia del servicio (criterio 4) */
    private UrgencyLevel urgencylevel;

    /** Indica si ya se generó una orden de trabajo desde este diagnóstico (criterio 7) */
    private boolean ordergenerated;

    /** Mecánico o administrador que registró el diagnóstico */
    private String registeredby;

    /** Fecha y hora de registro del diagnóstico */
    private LocalDateTime registrationdate;

    /** Fecha y hora de la última actualización (criterio 6) */
    private LocalDateTime updatedate;
}
