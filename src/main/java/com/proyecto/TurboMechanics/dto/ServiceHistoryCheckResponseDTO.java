package com.proyecto.TurboMechanics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Informa, antes de eliminar un servicio del catálogo, si ya tiene garantías
 * asociadas, para poder advertir al usuario en el frontend sin bloquear la
 * eliminación (mismo patrón que SparePartHistoryCheckResponseDTO).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceHistoryCheckResponseDTO {
    private boolean tieneGarantias;
    private int cantidadGarantias;
}