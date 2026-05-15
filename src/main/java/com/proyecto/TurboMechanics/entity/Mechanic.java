package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import com.proyecto.TurboMechanics.enums.LaborStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "mecanicos")
public class Mechanic {

    /** Id del mecánico */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mecanico")
    private Long id;

    /** Nombre completo del mecánico */
    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 150)
    private String name;

    /** Número de documento de identidad */
    @NotNull(message = "El documento es obligatorio")
    @Column(name = "documento", nullable = false, unique = true)
    private Long document;

    /** Cargo o especialidad del mecánico */
    @NotBlank(message = "El cargo es obligatorio")
    @Column(name = "cargo", nullable = false, length = 100)
    private String position;

    /** Fecha de ingreso al taller */
    @NotNull(message = "La fecha de ingreso es obligatoria")
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate hireDate;

    /** Teléfono de contacto (opcional) */
    @Pattern(regexp = "^[0-9]{7,15}$", message = "Teléfono inválido")
    @Column(name = "telefono", length = 20)
    private String phone;

    /** Correo electrónico de contacto (opcional) */
    @Email(message = "Correo inválido")
    @Column(name = "correo", length = 150)
    private String email;

    /** Salario del mecánico (opcional) */
    @DecimalMin(value = "0.0", inclusive = false, message = "El salario debe ser mayor a cero")
    @Column(name = "salario", precision = 12, scale = 2)
    private BigDecimal salary;

    /** Estado laboral del mecánico */
    @NotNull(message = "El estado laboral es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_laboral", nullable = false, length = 20)
    private LaborStatus laborStatus = LaborStatus.ACTIVO;

    /** Capacidad máxima de órdenes activas que puede tener el mecánico */
    @Column(name = "capacidad_maxima_ordenes")
    private Integer maxOrderCapacity = 3;

    /** Usuario que registró al mecánico */
    @Column(name = "creado_por", length = 100)
    private String createdBy;

    /** Fecha de creación del registro */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Fecha de la última actualización */
    @Column(name = "fecha_actualizacion")
    private LocalDateTime updatedAt;

    /** Usuario que realizó la última actualización */
    @Column(name = "actualizado_por", length = 100)
    private String updatedBy;

    /** Historial de ausencias del mecánico */
    @OneToMany(mappedBy = "mechanic", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MechanicAbsence> absences = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (laborStatus == null) laborStatus = LaborStatus.ACTIVO;
        if (maxOrderCapacity == null) maxOrderCapacity = 3;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}