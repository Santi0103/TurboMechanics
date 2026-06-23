package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserProfileRequestDTO {

    /** Nombre del usuario (debe ser texto, no solo números) */
    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El nombre no puede ser solo números o símbolos")
    private String username;

    /** Cédula/identificación (solo números, longitud razonable) */
    @NotNull(message = "La cédula es obligatoria")
    @Min(value = 10000, message = "La identificación debe tener al menos 5 dígitos")
    @Max(value = 999999999, message = "La identificación es demasiado larga")
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