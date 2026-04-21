package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "usuarios")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "nombre")
    private String username;
    @Column(name = "cedula")
    private Integer identification;
    @Column(name = "telefono")
    private String phone;
    @Column(name = "correo")
    private String email;
    @Column(name = "contraseña")
    private String password;
    @Column(name = "rol_id")
    private Long rolId;
}
