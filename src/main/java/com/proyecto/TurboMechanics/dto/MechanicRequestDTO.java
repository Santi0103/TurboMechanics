package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import com.proyecto.TurboMechanics.enums.LaborStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class MechanicRequestDTO {

    /** Nombre completo del mecánico (obligatorio) */
    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    /** Número de documento de identidad (obligatorio) */
    @NotNull(message = "El documento es obligatorio")
    private Long document;

    /** Cargo o especialidad del mecánico (obligatorio) */
    @NotBlank(message = "El cargo es obligatorio")
    private String position;

    /** Fecha de ingreso al taller (obligatoria) */
    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate hireDate;

    /** Teléfono de contacto (opcional) */
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Teléfono inválido")
    private String phone;

    /** Correo electrónico de contacto (opcional) */
    @Email(message = "Correo inválido")
    private String email;

    /** Salario del mecánico (opcional) */
    @DecimalMin(value = "0.0", inclusive = false, message = "El salario debe ser mayor a cero")
    private BigDecimal salary;

    /** Estado laboral (opcional, por defecto ACTIVO) */
    private LaborStatus laborStatus;
}