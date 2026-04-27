package com.proyecto.TurboMechanics.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PopularServiceResponseDTO {
    
    /**id del servicio */
    private Long serviceId;

    /**nonbre del servicio */
    private String name;

    /**total de aplicaciones */
    private Long totalApplications;
}
