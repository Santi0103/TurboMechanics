package com.proyecto.TurboMechanics.dto;

import lombok.Data;
 
@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private Integer identification;
    private String phone;
    private String email;
    private Long rolId;
}