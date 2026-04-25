package com.proyecto.TurboMechanics.dto;

import lombok.Data;
 
@Data
public class UserResponseDTO {

    /** id del cliente */
    private Long id;

    /** nombre del cliente */
    private String username;

    /**identificacion del cliente */
    private Integer identification;

    /** telefono del cliente */
    private String phone;

    /** correo del cliente */
    private String email;

    /** RolId del cliente */
    private Long rolId;
}