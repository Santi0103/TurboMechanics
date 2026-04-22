package com.proyecto.TurboMechanics.dto;

import lombok.Data;

@Data
public class RefreshTokenResponseDTO {
    private String message;
    private String jwt;
}
