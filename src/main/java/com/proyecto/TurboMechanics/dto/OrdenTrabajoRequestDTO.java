package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.entity.OrdenTrabajo;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class OrdenTrabajoRequestDTO {

    // Cliente
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 150)
    private String clienteNombre;

    @NotBlank(message = "La identificación del cliente es obligatoria")
    @Size(max = 20)
    private String clienteIdentificacion;

    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s\\-]{7,20}$", message = "Formato de teléfono inválido")
    private String clienteTelefono;

    // Vehículo
    @NotBlank(message = "La placa del vehículo es obligatoria")
    @Size(max = 10)
    private String vehiculoPlaca;

    @NotBlank(message = "La marca del vehículo es obligatoria")
    @Size(max = 50)
    private String vehiculoMarca;

    @NotBlank(message = "El modelo del vehículo es obligatorio")
    @Size(max = 50)
    private String vehiculoModelo;

    @NotNull(message = "El año del vehículo es obligatorio")
    @Min(value = 1900, message = "Año inválido")
    @Max(value = 2100, message = "Año inválido")
    private Integer vehiculoAnio;

    @Size(max = 30)
    private String vehiculoColor;

    // Fallas
    @NotBlank(message = "Las fallas reportadas son obligatorias")
    private String fallasReportadas;

    @FutureOrPresent(message = "La fecha estimada no puede ser en el pasado")
    private LocalDate fechaEntregaEstimada;

    // Estado vehículo
    private OrdenTrabajo.NivelCombustible nivelCombustible;
    private OrdenTrabajo.EstadoCondicion  estadoRayones;
    private OrdenTrabajo.EstadoCondicion  estadoAbolladuras;
    private String accesoriosObservaciones;

    // Control
    private OrdenTrabajo.Prioridad prioridad;
    private String creadoPor;
}
