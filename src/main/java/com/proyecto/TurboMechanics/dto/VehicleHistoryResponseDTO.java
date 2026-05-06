package com.proyecto.TurboMechanics.dto;

import java.util.List;
import lombok.Data;

@Data
public class VehicleHistoryResponseDTO {
    
    /** Placa del vehículo consultado */
    private String vehicleplate;

    /** Marca del vehículo (tomada del primer registro si existe) */
    private String vehiclebrand;

    /** Modelo del vehículo (tomado del primer registro si existe) */
    private String vehiclemodel;

    /** Año del vehículo (tomado del primer registro si existe) */
    private Integer vehicleyear;

    /** Total de servicios encontrados */
    private int totalServices;

    /**
     * mensaje claro cuando no hay historial.
     * "No se encontraron servicios previos para este vehículo."
     */
    private String message;

    /** Historial de servicios del vehículo, con detalles de cada orden de trabajo, incluyendo
     * cada una con fecha, servicio, repuestos y estado final.
     */
    private List<WorkOrderResponseDTO> history;
}
