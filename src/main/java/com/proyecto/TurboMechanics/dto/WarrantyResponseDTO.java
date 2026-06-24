package com.proyecto.TurboMechanics.dto;

import lombok.Data;
import com.proyecto.TurboMechanics.enums.WarrantyStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class WarrantyResponseDTO {

    /** Id de la garantía */
    private Long id;

    /** Número de comprobante generado */
    private String voucherNumber;

    /** Id de la orden de trabajo y datos relacionados */
    private Long workOrderId;

    /** Número de la orden de trabajo */
    private String workOrderNumber;

    /** Nombre del cliente */
    private String clientName;

    /** Documento de identificación del cliente */
    private String clientIdentification;

    /** Placa del vehículo */
    private String vehiclePlate;

    /** Servicios cubiertos por la garantía (puede haber varios) */
    private List<ServiceCoverageItemDTO> services;

    /** Repuestos cubiertos por la garantía (puede haber varios) */
    private List<SparePartCoverageItemDTO> spareParts;

    /** Texto corto con todos los nombres cubiertos, separados por coma (para mostrar en tablas) */
    private String coverageSummary;

    /** Fecha de inicio de la vigencia */
    private LocalDate startDate;

    /** Fecha de fin de la vigencia */
    private LocalDate endDate;

    /** Estado calculado de la garantía (ACTIVA, VENCIDA, CERRADA) */
    private WarrantyStatus status;

    /** Observaciones o condiciones de la garantía */
    private String observations;

    /** Motivo del cierre de la garantía */
    private String closureReason;

    /** Fecha en que se cerró la garantía */
    private LocalDateTime closureDate;

    /** Usuario que realizó el cierre */
    private String closedBy;

    /** Fecha en que se generó el comprobante */
    private LocalDateTime voucherGeneratedAt;

    /** Usuario que generó el comprobante */
    private String voucherGeneratedBy;

    /** Usuario que creó la garantía */
    private String createdBy;

    /** Fecha de creación de la garantía */
    private LocalDateTime createdAt;

    /** Usuario que realizó la última modificación */
    private String updatedBy;

    /** Fecha de la última modificación */
    private LocalDateTime updatedAt;
}