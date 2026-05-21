package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.enums.QualityCheckStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class QualityCheckResponseDTO {

    /** Id del control de calidad */
    private Long id;

    /** Id de la orden de trabajo */
    private Long workOrderId;

    /** Número de la orden de trabajo */
    private String workOrderNumber;

    /** Placa del vehículo */
    private String vehiclePlate;

    /** Estado del control (EN_PROCESO, COMPLETADO, APROBADO, RECHAZADO) */
    private QualityCheckStatus status;

    /** Observaciones generales */
    private String observations;

    /** Lista de ítems de verificación */
    private List<QualityCheckItemResponseDTO> items;

    /** Total de ítems */
    private Integer totalItems;

    /** Ítems verificados */
    private Integer verifiedItems;

    /** Usuario que inició el control */
    private String createdBy;

    /** Fecha de inicio */
    private LocalDateTime createdAt;

    /** Usuario que aprobó o rechazó */
    private String approvedBy;

    /** Fecha de aprobación o rechazo */
    private LocalDateTime approvedAt;

    /** Fecha de última actualización */
    private LocalDateTime updatedAt;

    /** Usuario de última actualización */
    private String updatedBy;
}