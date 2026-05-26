package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequestDTO {

    /** ID de la orden de trabajo finalizada que se quiere reseñar */
    @NotNull(message = "El ID de la orden de trabajo es obligatorio")
    private Long workOrderId;

    /** Comentario escrito por el cliente */
    @NotBlank(message = "El comentario no puede estar vacío")
    @Size(min = 10, max = 1000, message = "El comentario debe tener entre 10 y 1000 caracteres")
    private String comment;

    /** Calificación de 1 a 5 estrellas */
    @NotNull(message = "La calificación es obligatoria")
    @Min(value = 1, message = "La calificación mínima es 1")
    @Max(value = 5, message = "La calificación máxima es 5")
    private Integer rating;
}