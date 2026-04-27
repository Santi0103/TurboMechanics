package com.proyecto.TurboMechanics.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PopularSpacePartsResponseDTO {

    /**repuesto id */
    private Long spacePartsId;

    /**nombre del repuesto */
    private String name;

    /**referencia del repuesto */
    private String reference;

    /**total de salidas del repuesto */
    private Long totalOutput;
}
