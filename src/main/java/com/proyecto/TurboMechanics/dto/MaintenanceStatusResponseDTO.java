package com.proyecto.TurboMechanics.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.entity.WorkOrder.StateOrder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceStatusResponseDTO {


    /** ID numérico de la orden de trabajo */
    private Long workOrderId;
    
    /** Número de orden de trabajo */
    private String numberOrder;

    /**
     * estado de la orden, RECIBIDO | EN_DIAGNOSTICO | EN_REPARACION | LISTO |
     * ENTREGADO | CANCELADO
     */
    private StateOrder stateOrder;

    /** Nombre del mecánico asignado. */
    private String assignedMechanicName;

    /** descripción del servicio solicitado */
    private String serviceDescription;

    /** Fecha y hora de ingreso del vehículo */
    private LocalDateTime dateEntry;

    /** Fecha estimada de entrega. */
    private LocalDate estimatedDelivery;

    /** Marca del vehículo */
    private String vehicleBrand;

    /** Modelo del vehículo */
    private String vehicleModel;

    /** Placa del vehículo */
    private String vehiclePlate;
}