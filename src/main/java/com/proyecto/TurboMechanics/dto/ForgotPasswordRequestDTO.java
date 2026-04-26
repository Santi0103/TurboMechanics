package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {
    
    /**
     * Correo electrónico o número de WhatsApp registrado del usuario.
     * Se acepta cualquiera de los dos (criterio 1 de la HU).
     */
    @NotBlank(message = "El correo o número de WhatsApp es obligatorio")
    private String emailOrPhone;
}
