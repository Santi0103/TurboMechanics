package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

@Entity
@Data
@Table(
    name = "usuarios",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "correo")
    }
)
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "the name is required")
    @Column(name = "nombre")
    private String username;

    @NotNull(message = "La cédula es obligatoria")
    @Column(name = "cedula")
    private Integer identification;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Teléfono inválido")
    @Column(name = "telefono")
    private String phone;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Correo inválido")
    @Column(name = "correo", unique = true)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "Mínimo 6 caracteres")
    @Column(name = "password")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    @Column(name = "rol_id", nullable = false)
    private Long rolId;
}