package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDTO {
    
    /** Correo electrónico o número de WhatsApp con el que solicitó el código */
    @NotBlank(message = "El correo o número de WhatsApp es obligatorio")
    private String emailOrPhone;

    /** Código de 6 dígitos recibido por correo o WhatsApp */
    @NotBlank(message = "El código de recuperación es obligatorio")
    @Size(min = 6, max = 6, message = "El código debe tener exactamente 6 dígitos")
    private String code;

    /** Nueva contraseña que el usuario quiere establecer */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String newPassword;
}
