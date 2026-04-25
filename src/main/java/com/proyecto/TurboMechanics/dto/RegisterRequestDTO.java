package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    /** nombre del cliente */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /** identificacion del cliente */
    @NotNull(message = "Identification is required")
    private Integer identification; 

    /** telefono del cliente */
    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "\\d+", message = "Phone must contain only numbers")
    private String phone;

    /** correo del cliente */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    /** contraseña del cliente */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 6 characters")
    private String password;
}
