package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ordenes_trabajo")
public class WorkOrder {

    /** El ID de la orden de trabajo */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** El número de orden, generado automáticamente al crear la orden */
    @Column(name = "numero_orden", unique = true, nullable = false, length = 20)
    private String numberorder;

    /** El nombre del cliente */
    @Column(name = "cliente_nombre", nullable = false, length = 150)
    private String clientname;

    /** La identificación del cliente */
    @Column(name = "cliente_identificacion", nullable = false, length = 20)
    private String clientidentification;

    /** El número de teléfono del cliente */
    @Column(name = "cliente_telefono", nullable = false, length = 20)
    private String clientphone;

    /** La placa del vehículo */
    @Column(name = "vehiculo_placa", nullable = false, length = 10)
    private String vehicleplate;

    /** La marca del vehículo */
    @Column(name = "vehiculo_marca", nullable = false, length = 50)
    private String vehiclebrand;

    /** El modelo del vehículo */
    @Column(name = "vehiculo_modelo", nullable = false, length = 50)
    private String vehiclemodel;

    /** El año del vehículo */
    @Column(name = "vehiculo_anio", nullable = false)
    private Integer vehicleyear;

    /** El color del vehículo */
    @Column(name = "vehiculo_color", length = 30)
    private String vehiclecolor;

    /** Las fallas reportadas */
    @Column(name = "fallas_reportadas", nullable = false, columnDefinition = "TEXT")
    private String failuresreported;

    /** La fecha de ingreso */
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime dateentry;

    /** La fecha estimada de entrega */
    @Column(name = "fecha_entrega_estimada")
    private LocalDate dateestimateddelivery;

    /** El nivel de combustible al ingreso */
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_combustible", length = 20)
    private LevelFuel levelfuel;

    /** El estado de las rayas */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_rayones", length = 20)
    private StateCondition statescratches;

    /** El estado de los dientes */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_abolladuras", length = 20)
    private StateCondition statedents;

    /** Las observaciones sobre los accesorios */
    @Column(name = "accesorios_observaciones", columnDefinition = "TEXT")
    private String accessoriesobservations;

    /** El estado de la orden */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_orden", nullable = false, length = 20)
    private StateOrder stateorder = StateOrder.RECIBIDO;

    /** La prioridad de la orden */
    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", length = 20)
    private Priority priority = Priority.NORMAL;

    /** El usuario que creó la orden */
    @Column(name = "creado_por", length = 100)
    private String createdBy;

    /** La fecha de creación de la orden */
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime datecreation;

    /** Método que se ejecuta antes de persistir la entidad */

    /** El motivo de cancelación */
    @Column(name = "motivo_cancelacion", columnDefinition = "TEXT")
    private String cancellationreason;

    /** La fecha de cancelación */
    @Column(name = "fecha_cancelacion")
    private LocalDateTime cancellationdate;

    @PrePersist
    protected void onCreate() {
        if (datecreation == null) datecreation = LocalDateTime.now();
        if (dateentry == null)  dateentry  = LocalDateTime.now();
        if (stateorder == null)   stateorder   = StateOrder.RECIBIDO;
        if (priority == null)     priority     = Priority.NORMAL;
    }

    /** Enumeraciones para los campos de la orden de trabajo */                                             
    public enum LevelFuel { VACIO, UN_CUARTO, MITAD, TRES_CUARTOS, LLENO }
    public enum StateCondition  { SIN_NOVEDAD, LEVE, MODERADO, SEVERO }
    public enum StateOrder      { RECIBIDO, EN_DIAGNOSTICO, EN_REPARACION, LISTO, ENTREGADO, CANCELADO }
    public enum Priority        { BAJA, NORMAL, ALTA, URGENTE }
}