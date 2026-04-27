package com.proyecto.TurboMechanics.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CriticalStockResponseDTO {

    /**id del repuesto */
    private Long spacePartsId;

    /**nonbre del repuesto */
    private String name;

    /** referencia del repuesto */
    private String reference;

    /**cantidad actual del repuesto */
    private Integer currentStock;

    /**cantidad minima del repuesto */
    private Integer stockMin;

    /**estado del repuesto */
    private String status;
}
