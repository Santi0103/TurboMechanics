package com.proyecto.TurboMechanics.dto;

import lombok.Data;

@Data
public class LoginResponseDTO extends MessageResponseDTO{
    private String jwt;
    private Long rolId;
}
