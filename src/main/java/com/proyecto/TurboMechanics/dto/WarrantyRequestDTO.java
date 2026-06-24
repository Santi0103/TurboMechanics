// ════════════════════════════════════════════════════════════════════════════
// DTO HU 8.1 / 8.3 — WarrantyRequestDTO
// Archivo: dto/WarrantyRequestDTO.java
// ════════════════════════════════════════════════════════════════════════════
package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class WarrantyRequestDTO {

    /** Id de la orden de trabajo (obligatorio) */
    @NotNull(message = "El id de la orden de trabajo es obligatorio")
    private Long workOrderId;

    /** Ids de los servicios cubiertos (puede haber varios, o ninguno si hay repuestos) */
    private List<Long> serviceIds;

    /** Ids de los repuestos cubiertos (puede haber varios, o ninguno si hay servicios) */
    private List<Long> sparePartIds;

    /** Fecha de inicio de vigencia (obligatoria) */
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate startDate;

    /** Fecha de fin de vigencia (obligatoria) */
    @NotNull(message = "La fecha de fin de vigencia es obligatoria")
    private LocalDate endDate;

    /** Observaciones o condiciones de la garantía (opcional) */
    private String observations;

    /** Valida que al menos un servicio o un repuesto esté informado */
    @AssertTrue(message = "Debe asociar la garantía a al menos un servicio o un repuesto")
    public boolean isAssociationValid() {
        return (serviceIds != null && !serviceIds.isEmpty())
                || (sparePartIds != null && !sparePartIds.isEmpty());
    }
}