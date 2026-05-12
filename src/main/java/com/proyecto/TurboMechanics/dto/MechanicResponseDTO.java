package com.proyecto.TurboMechanics.dto;

import lombok.Data;
import com.proyecto.TurboMechanics.enums.LaborStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MechanicResponseDTO {

    /** Id del mecánico */
    private Long id;

    /** Nombre completo del mecánico */
    private String name;

    /** Número de documento de identidad */
    private Long document;

    /** Cargo o especialidad del mecánico */
    private String position;

    /** Fecha de ingreso al taller */
    private LocalDate hireDate;

    /** Teléfono de contacto */
    private String phone;

    /** Correo electrónico de contacto */
    private String email;

    /** Salario del mecánico */
    private BigDecimal salary;

    /** Estado laboral del mecánico */
    private LaborStatus laborStatus;

    /** Usuario que registró al mecánico */
    private String createdBy;

    /** Fecha de creación del registro */
    private LocalDateTime createdAt;

    /** Fecha de la última actualización */
    private LocalDateTime updatedAt;

    /** Usuario que realizó la última actualización */
    private String updatedBy;
}