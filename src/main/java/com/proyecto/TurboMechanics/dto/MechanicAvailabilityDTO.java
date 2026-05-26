package com.proyecto.TurboMechanics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la disponibilidad de un mecánico para recibir órdenes de trabajo.
 * HU 6.7 — Asignar órdenes de trabajo a mecánicos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MechanicAvailabilityDTO {

    /** Id interno del mecánico */
    private Long id;

    /** Nombre completo del mecánico */
    private String name;

    /** Número de documento de identidad */
    private Long document;

    /** Cargo o especialidad */
    private String position;

    /** Estado laboral (siempre ACTIVO en esta lista) */
    private String laborStatus;

    /** Capacidad máxima de órdenes activas configurada para este mecánico */
    private int maxOrderCapacity;

    /** Cantidad de órdenes activas que tiene actualmente asignadas */
    private long currentOrderCount;

    /** true si currentOrderCount < maxOrderCapacity */
    private boolean available;
}