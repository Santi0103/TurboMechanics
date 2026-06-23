package com.proyecto.TurboMechanics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Un repuesto cubierto dentro de una garantía (puede haber varios por garantía) */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SparePartCoverageItemDTO {
    private Long id;
    private String name;
    private String reference;
    /** true si el repuesto ya fue eliminado del inventario (se muestra el snapshot guardado) */
    private boolean deleted;
}