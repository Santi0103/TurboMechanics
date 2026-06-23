package com.proyecto.TurboMechanics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Informa, antes de eliminar un repuesto, si ya tiene historial asociado
 * (ventas y/o garantías), para poder advertir al usuario en el frontend
 * sin bloquear la eliminación.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SparePartHistoryCheckResponseDTO {
    private boolean tieneVentas;
    private boolean tieneGarantias;
    private int cantidadVentas;
    private int cantidadGarantias;
}
