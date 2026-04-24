package com.proyecto.TurboMechanics.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "ordenes_trabajo")
public class OrdenTrabajo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_orden", unique = true, nullable = false, length = 20)
    private String numeroOrden;

    // --- Cliente ---
    @Column(name = "cliente_nombre", nullable = false, length = 150)
    private String clienteNombre;

    @Column(name = "cliente_identificacion", nullable = false, length = 20)
    private String clienteIdentificacion;

    @Column(name = "cliente_telefono", nullable = false, length = 20)
    private String clienteTelefono;

    // --- Vehículo ---
    @Column(name = "vehiculo_placa", nullable = false, length = 10)
    private String vehiculoPlaca;

    @Column(name = "vehiculo_marca", nullable = false, length = 50)
    private String vehiculoMarca;

    @Column(name = "vehiculo_modelo", nullable = false, length = 50)
    private String vehiculoModelo;

    @Column(name = "vehiculo_anio", nullable = false)
    private Integer vehiculoAnio;

    @Column(name = "vehiculo_color", length = 30)
    private String vehiculoColor;

    // --- Fallas ---
    @Column(name = "fallas_reportadas", nullable = false, columnDefinition = "TEXT")
    private String fallasReportadas;

    // --- Fechas ---
    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "fecha_entrega_estimada")
    private LocalDate fechaEntregaEstimada;

    // --- Estado al ingreso ---
    @Enumerated(EnumType.STRING)
    @Column(name = "nivel_combustible", length = 20)
    private NivelCombustible nivelCombustible;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_rayones", length = 20)
    private EstadoCondicion estadoRayones;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_abolladuras", length = 20)
    private EstadoCondicion estadoAbolladuras;

    @Column(name = "accesorios_observaciones", columnDefinition = "TEXT")
    private String accesoriosObservaciones;

    // --- Control ---
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_orden", nullable = false, length = 20)
    private EstadoOrden estadoOrden = EstadoOrden.RECIBIDO;

    @Enumerated(EnumType.STRING)
    @Column(name = "prioridad", length = 20)
    private Prioridad prioridad = Prioridad.NORMAL;

    @Column(name = "creado_por", length = 100)
    private String creadoPor;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) fechaCreacion = LocalDateTime.now();
        if (fechaIngreso == null)  fechaIngreso  = LocalDateTime.now();
        if (estadoOrden == null)   estadoOrden   = EstadoOrden.RECIBIDO;
        if (prioridad == null)     prioridad     = Prioridad.NORMAL;
    }

    public enum NivelCombustible { VACIO, UN_CUARTO, MITAD, TRES_CUARTOS, LLENO }
    public enum EstadoCondicion  { SIN_NOVEDAD, LEVE, MODERADO, SEVERO }
    public enum EstadoOrden      { RECIBIDO, EN_DIAGNOSTICO, EN_REPARACION, LISTO, ENTREGADO, CANCELADO }
    public enum Prioridad        { BAJA, NORMAL, ALTA, URGENTE }
}