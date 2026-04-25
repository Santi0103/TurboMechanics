package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserRequestDTO {
 
    /** nombre del cliente */
    @NotBlank(message = "The name cannot be empty")
    private String username;
 
    /** identificacion del cliente */
    @NotNull(message = "the identification cannot be empty")
    private Integer identification;
    
    /** telefono del cleinte */
    @NotBlank(message = "the phone cannot be empty")
    private String phone;
    
    /** correo del cliente */
    @Email(message = "The email address is not in a valid format.")
    @NotBlank(message = "The email cannot be empty.")
    private String email;
}
