package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    /** nombre del cliente (debe ser texto, no solo números) */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "Username must contain letters, not only numbers or symbols")
    private String username;

    /** identificacion del cliente (solo números, longitud razonable de cédula) */
    @NotNull(message = "Identification is required")
    @Min(value = 10000, message = "Identificacion es demasiado corta")
    @Max(value = 999999999, message = "Identificacion es demasiado larga")
    private Integer identification;

    /** telefono del cliente (solo números, 7 a 15 dígitos) */
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Phone must contain only numbers (7 to 15 digits)")
    private String phone;

    /** correo del cliente */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /** dirección del cliente */
    @NotBlank(message = "Address is required")
    private String address;

    /** contraseña del cliente */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}