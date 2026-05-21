package com.proyecto.TurboMechanics.dto;

import lombok.Data;

@Data
public class QualityCheckItemRequestDTO {

    /** Id del ítem a actualizar */
    private Long itemId;

    /** Indica si el servicio fue verificado satisfactoriamente */
    private Boolean verified;

    /** Observación específica del ítem (opcional) */
    private String observation;
}