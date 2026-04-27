package com.proyecto.TurboMechanics.dto;

import java.time.LocalDateTime;

import com.proyecto.TurboMechanics.enums.MovementType;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MovementsResponseDTO {

    /**id del movimiento */
    private Long id;

    /**repuestoid del movimiento */
    private Long spacePartsId;

    /**nombre del repuesto */
    private String spacePartsName;

    /**tipo de movimiento */
    private MovementType type;

    /**cantidad del movimiento */
    private Integer stock;

    /**fecha del movimientp */
    private LocalDateTime date;

    /**motivo del movimiento */
    private String motive;
}
