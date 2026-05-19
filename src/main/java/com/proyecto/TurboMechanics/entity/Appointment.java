package com.proyecto.TurboMechanics.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import com.proyecto.TurboMechanics.enums.StatusAppointment;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "citas")
public class Appointment {

    /** Id de la cita */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cliente que agenda la cita */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_cedula", referencedColumnName = "cedula", nullable = false)
    private Users users;

    /** Vehículo asociado a la cita */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehiculo_placa", referencedColumnName = "plate", nullable = false)
    private Vehicle vehicle;

    /** Fecha de la cita */
    @Column(nullable = false)
    private LocalDate date;

    /** Hora de la cita */
    @Column(nullable = false)
    private LocalTime time;

    /** Motivo o descripción del servicio requerido */
    @Column(columnDefinition = "TEXT")
    private String reason;

    /** Estado de la cita */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusAppointment status;

    /** Usuario que registró o gestionó la cita */
    @Column(name = "registrado_por")
    private String createdBy;
}
