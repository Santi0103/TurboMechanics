package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    
    @NotBlank(message = "The email cannot be empty.")
    private String email;

    @NotBlank(message = "The password cannot be empty.")
    private String password;
}
