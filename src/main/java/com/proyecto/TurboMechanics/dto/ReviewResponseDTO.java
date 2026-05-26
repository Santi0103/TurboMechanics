package com.proyecto.TurboMechanics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewResponseDTO {

    //** ID de la reseña */
    private Long id;

    /** ID del usuario que escribió la reseña */
    private Long userId;

    /** Nombre de usuario que escribió la reseña */
    private String username;

    /** ID de la orden de trabajo asociada */
    private Long workOrderId;

    /** Número de orden de trabajo asociada (para mostrar en la reseña) */
    private String workOrderNumber;

    /** Comentario escrito por el cliente */
    private String comment;

    /** Calificación de 1 a 5 estrellas */
    private Integer rating;

    /** Fecha y hora en que se registró la reseña */
    private LocalDateTime reviewDate;
}