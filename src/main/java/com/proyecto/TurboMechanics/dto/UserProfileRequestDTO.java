package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileRequestDTO {

    /** Nombre del usuario */
    @NotBlank(message = "El nombre es obligatorio")
    private String username;

    /** Cédula/identificación */
    @NotNull(message = "La cédula es obligatoria")
    private Integer identification;

    /** Teléfono */
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Teléfono inválido")
    private String phone;

    /** Correo electrónico */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    private String email;
}