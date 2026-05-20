// ════════════════════════════════════════════════════════════════════════════
// DTO HU 8.1 / 8.3 — WarrantyRequestDTO
// Archivo: dto/WarrantyRequestDTO.java
// ════════════════════════════════════════════════════════════════════════════
package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WarrantyRequestDTO {

    /** Id de la orden de trabajo (obligatorio) */
    @NotNull(message = "El id de la orden de trabajo es obligatorio")
    private Long workOrderId;

    /** Id del servicio cubierto (opcional si se informa repuesto) */
    private Long serviceId;

    /** Id del repuesto cubierto (opcional si se informa servicio) */
    private Long sparePartId;

    /** Fecha de inicio de vigencia (obligatoria) */
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate startDate;

    /** Fecha de fin de vigencia (obligatoria) */
    @NotNull(message = "La fecha de fin de vigencia es obligatoria")
    private LocalDate endDate;

    /** Observaciones o condiciones de la garantía (opcional) */
    private String observations;

    /** Valida que al menos uno de los dos (servicio o repuesto) esté informado */
    @AssertTrue(message = "Debe asociar la garantía a un servicio o a un repuesto")
    public boolean isAssociationValid() {
        return serviceId != null || sparePartId != null;
    }
}