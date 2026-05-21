package com.proyecto.TurboMechanics.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class QualityCheckItemResponseDTO {

    /** Id del ítem */
    private Long id;

    /** Nombre del servicio a verificar */
    private String serviceName;

    /** Id del servicio (referencia) */
    private Long serviceId;

    /** Indica si fue verificado */
    private Boolean verified;

    /** Observación específica */
    private String observation;

    /** Usuario que lo verificó */
    private String verifiedBy;

    /** Fecha de verificación */
    private LocalDateTime verifiedAt;
}