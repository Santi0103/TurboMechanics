package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.entity.WorkOrder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WorkOrderResponseDTO {

    /** El ID de la orden de trabajo */
    private Long id;

    /** El número de orden, generado automáticamente al crear la orden */
    private String numberorder;

    /** El nombre del cliente */
    private String clientname;

    /** La identificación del cliente */
    private String clientidentification;

    /** El número de teléfono del cliente */
    private String clientphone;

    /** La placa del vehículo */
    private String vehicleplate;

    /** La marca del vehículo */
    private String vehiclebrand;

    /** El modelo del vehículo */
    private String vehiclemodel;

    /** El año del vehículo */
    private Integer vehicleyear;

    /** El color del vehículo */
    private String vehiclecolor;

    /** Las fallas reportadas */
    private String failuresreported;

    /** La fecha de entrada */
    private LocalDateTime dateentry;

    /** La fecha estimada de entrega */
    private LocalDate dateestimateddelivery;

    /** El nivel de combustible */
    private WorkOrder.LevelFuel levelfuel;

    /** El estado de las rayas */
    private WorkOrder.StateCondition  statescratches;

    /** El estado de los dientes */
    private WorkOrder.StateCondition  statedents;

    /** Las observaciones sobre los accesorios */
    private String accessoriesobservations;

    /** El estado de la orden */
    private WorkOrder.StateOrder stateorder;

    /** La prioridad de la orden */
    private WorkOrder.Priority   priority;

    /** El usuario que creó la orden */
    private String createdBy;

    /** La fecha de creación de la orden */
    private LocalDateTime datecreation;

    /** El motivo de cancelación */
    private String cancellationreason;

    /** La fecha de cancelación */
    private LocalDateTime cancellationdate;

    // ── HU 6.7 — Asignación de mecánico ──────────────────────────────────────

    /** Nombre del mecánico asignado a esta orden (null si no tiene) */
    private String assignedMechanicName;

    /** Documento del mecánico asignado (null si no tiene) */
    private Long assignedMechanicDocument;
}