package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class SparePartsResponseDTO {
    private Long id;
    private String name;
    private String reference;
    private Integer stock;
    private Integer stockMin;
    private BigDecimal price;
    private String category;
    private String statusStock;
}
