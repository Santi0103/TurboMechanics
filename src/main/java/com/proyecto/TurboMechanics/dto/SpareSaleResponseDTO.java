package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpareSaleResponseDTO {
    private Long id;
    private String sparePartName;
    private String sparePartReference;
    private String sparePartCategory;
    private boolean sparePartDeleted;
    private String payerEmail;
    private BigDecimal price;
    private String externalReference;
    private String preferenceId;
    private LocalDateTime createdAt;
    private String status;
}