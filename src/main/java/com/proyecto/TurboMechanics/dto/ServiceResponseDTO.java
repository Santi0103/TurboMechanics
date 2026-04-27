package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServiceResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean active;
}
