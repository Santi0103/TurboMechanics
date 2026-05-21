package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.enums.WarrantyValidationStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class WarrantyValidationResponseDTO {

    /** Id de la validación registrada */
    private Long validationId;

    /** Id de la garantía validada */
    private Long warrantyId;

    /** Número de comprobante de la garantía */
    private String voucherNumber;

    /** Nombre del cliente */
    private String clientName;

    /** Placa del vehículo */
    private String vehiclePlate;

    /** Servicio o repuesto cubierto */
    private String coverageDescription;

    /** Fecha de inicio de vigencia */
    private LocalDate startDate;

    /** Fecha de fin de vigencia */
    private LocalDate endDate;

    /** Resultado de la validación (VIGENTE, VENCIDA, CERRADA) */
    private WarrantyValidationStatus result;

    /** Indica si la cobertura fue aprobada */
    private Boolean coverageApproved;

    /** Mensaje informativo del resultado */
    private String message;

    /** Motivo de rechazo si no se aprobó */
    private String rejectionReason;

    /** Usuario que realizó la validación */
    private String validatedBy;

    /** Fecha y hora de la validación */
    private LocalDateTime validatedAt;
}