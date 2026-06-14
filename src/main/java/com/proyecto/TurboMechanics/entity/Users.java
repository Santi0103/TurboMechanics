package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
@Table(name = "usuarios")
public class Users {

    /** Id del cliente */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    /** nombre del cliente */
    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre")
    private String username;

    /** identificacion del cliente */
    @NotNull(message = "La cédula es obligatoria")
    @Column(name = "cedula")
    private Integer identification;

    /** telefono del cliente */
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Teléfono inválido")
    @Column(name = "telefono")
    private String phone;

    /** correo del cliente */
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    @Column(name = "correo", unique = true)
    private String email;

    /** dirección del cliente */
    @NotBlank(message = "La dirección es obligatoria")
    @Column(name = "direccion")
    private String address;

    /** contraseña del cliente */
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "Mínimo 8 caracteres")
    @Column(name = "contrasena")
    private String password;

    /** rolId del cliente */
    @NotNull(message = "El rol es obligatorio")
    @Column(name = "rol", nullable = false)
    private Long rolId;

    /** Código de recuperación de contraseña (6 dígitos, expira en 15 min) */
    @Column(name = "reset_code", length = 6)
    private String resetCode;

    /** Fecha y hora de expiración del código de recuperación */
    @Column(name = "reset_code_expiry")
    private java.time.LocalDateTime resetCodeExpiry;
}
