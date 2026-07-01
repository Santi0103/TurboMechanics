package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRequestDTO {
 
    /** nombre del cliente (debe ser texto, no solo números) */
    @NotBlank(message = "The name cannot be empty")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "The name must contain letters, not only numbers or symbols")
    private String username;
 
    /** identificacion del cliente (solo números, longitud razonable) */
    @NotNull(message = "the identification cannot be empty")
    @Min(value = 10000, message = "Identification must have at least 5 digits")
    @Max(value = 999999999999999L, message = "La identificación debe tener máximo 15 dígitos")
    private Integer identification;
    
    /** telefono del cliente (solo números, 7 a 15 dígitos) */
    @NotBlank(message = "the phone cannot be empty")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Phone must contain only numbers (7 to 15 digits)")
    private String phone;
    
    /** correo del cliente */
    @Email(message = "The email address is not in a valid format.")
    @NotBlank(message = "The email cannot be empty.")
    private String email;
}