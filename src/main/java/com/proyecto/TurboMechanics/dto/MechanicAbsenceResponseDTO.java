package com.proyecto.TurboMechanics.dto;

import lombok.Data;
import com.proyecto.TurboMechanics.enums.AbsenceType;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para ausencias de mecánicos (HU 6.8).
 */
@Data
public class MechanicAbsenceResponseDTO {

    /** Id de la ausencia */
    private Long id;

    /** Id del mecánico */
    private Long mechanicId;

    /** Nombre del mecánico */
    private String mechanicName;

    /** Documento del mecánico */
    private Long mechanicDocument;

    /** Fecha y hora de inicio de la ausencia */
    private LocalDateTime startDate;

    /** Fecha y hora de fin de la ausencia */
    private LocalDateTime endDate;

    /** Motivo de la ausencia */
    private String reason;

    /** Tipo de ausencia */
    private AbsenceType absenceType;

    /** Usuario que registró la ausencia */
    private String registeredBy;

    /** Fecha en que se registró la ausencia */
    private LocalDateTime registeredAt;
}