package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceResponseDTO {

    /**id del servicio */
    private Long id;

    /**nombre del servicio */
    private String name;

    /**descripcion del servicio */
    private String description;

    /**precio del servicio */
    private BigDecimal price;

    /**activo o inactivo del servicio */
    private Boolean active;
}
