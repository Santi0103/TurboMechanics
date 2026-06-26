package com.proyecto.TurboMechanics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Un servicio cubierto dentro de una garantía (puede haber varios por garantía) */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCoverageItemDTO {
    private Long id;
    private String name;
}