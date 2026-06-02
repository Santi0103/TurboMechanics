package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SparePartsResponseDTO {

    /**id de los repuestos */
    private Long id;

    /**nombre del repuesto */
    private String name;

    /** referencia del repuesto */
    private String reference;

    /** stock del repuesto */
    private Integer stock;

    /**stock minimo del repuesto */
    private Integer stockMin;

    /** precio del repuesto */
    private BigDecimal price;

    /**categoria del repuesto */
    private String category;

    /**estado del stock del repuesto */
    private String statusStock;
}
