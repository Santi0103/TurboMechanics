package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
@Table(name = "usuarios")
public class Users {

    /**Id del cliente */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**nombre del cliente */
    @NotBlank(message = "the name is required")
    @Column(name = "nombre")
    private String username;

    /**identificacion del cliente */
    @NotNull(message = "La cédula es obligatoria")
    @Column(name = "cedula")
    private Integer identification;

    /**telefono del cliente */
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Teléfono inválido")
    @Column(name = "telefono")
    private String phone;

    /**correo del cliente */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    @Column(name = "correo", unique = true)
    private String email;

    /**contraseña del cliente */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "Mínimo 6 caracteres")
    @Column(name = "password")
    private String password;

    /** rolId del clienet */
    @NotNull(message = "El rol es obligatorio")
    @Column(name = "rol_id", nullable = false)
    private Long rolId;

    /** Código de recuperación de contraseña (6 dígitos, expira en 15 min) */
    @Column(name = "reset_code", length = 6)
    private String resetCode;

    /** Fecha y hora de expiración del código de recuperación */
    @Column(name = "reset_code_expiry")
    private java.time.LocalDateTime resetCodeExpiry;
}