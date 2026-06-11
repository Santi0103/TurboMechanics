package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangepasswordRequestdto {

    /**contrasena actual del usuario */
    @NotBlank(message = "La contraseña actual es obligatoria")
    private String currentPassword;

    /**nueva contrasena del usuario */
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 6, message = "Mínimo 6 caracteres")
    private String newPassword;
}