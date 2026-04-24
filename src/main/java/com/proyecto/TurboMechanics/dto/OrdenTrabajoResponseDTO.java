package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.entity.OrdenTrabajo;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrdenTrabajoResponseDTO {

    private Long id;
    private String numeroOrden;

    // Cliente
    private String clienteNombre;
    private String clienteIdentificacion;
    private String clienteTelefono;

    // Vehículo
    private String vehiculoPlaca;
    private String vehiculoMarca;
    private String vehiculoModelo;
    private Integer vehiculoAnio;
    private String vehiculoColor;

    // Fallas y fechas
    private String fallasReportadas;
    private LocalDateTime fechaIngreso;
    private LocalDate fechaEntregaEstimada;

    // Estado vehículo
    private OrdenTrabajo.NivelCombustible nivelCombustible;
    private OrdenTrabajo.EstadoCondicion  estadoRayones;
    private OrdenTrabajo.EstadoCondicion  estadoAbolladuras;
    private String accesoriosObservaciones;

    // Control
    private OrdenTrabajo.EstadoOrden estadoOrden;
    private OrdenTrabajo.Prioridad   prioridad;
    private String creadoPor;
    private LocalDateTime fechaCreacion;
}
