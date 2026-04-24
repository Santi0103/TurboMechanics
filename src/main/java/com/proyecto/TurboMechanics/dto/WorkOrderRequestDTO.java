package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.entity.WorkOrder;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class WorkOrderRequestDTO {

    /** El nombre del cliente */
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 150)
    private String clientname;

    /** La identificación del cliente */
    @NotBlank(message = "La identificación del cliente es obligatoria")
    @Size(max = 20)
    private String clientidentification;

    /** El número de teléfono del cliente */
    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s\\-]{7,20}$", message = "Formato de teléfono inválido")
    private String clientphone;

    /** La placa del vehículo */
    @NotBlank(message = "La placa del vehículo es obligatoria")
    @Size(max = 10)
    private String vehicleplate;

    /** La marca del vehículo */
    @NotBlank(message = "La marca del vehículo es obligatoria")
    @Size(max = 50)
    private String vehiclebrand;

    /** El modelo del vehículo */
    @NotBlank(message = "El modelo del vehículo es obligatorio")
    @Size(max = 50)
    private String vehiclemodel;

    /** El año del vehículo */
    @NotNull(message = "El año del vehículo es obligatorio")
    @Min(value = 1900, message = "Año inválido")
    @Max(value = 2100, message = "Año inválido")
    private Integer vehicleyear;

    /** El color del vehículo */
    @Size(max = 30)
    private String vehiclecolor;

    /** Las fallas reportadas */
    @NotBlank(message = "Las fallas reportadas son obligatorias")
    private String failuresreported;

    /** La fecha estimada de entrega */
    @FutureOrPresent(message = "La fecha estimada no puede ser en el pasado")
    private LocalDate dateestimateddelivery;

    /** El nivel de combustible al ingreso */
    private WorkOrder.LevelFuel levelfuel;

    /** El estado de las rayas */
    private WorkOrder.StateCondition  statescratches;

    /** El estado de los dientes */
    private WorkOrder.StateCondition  statedents;

    /** Las observaciones sobre los accesorios */
    private String accessoriesobservations;

    /** La prioridad de la orden */
    private WorkOrder.Priority priority;

    /** El usuario que creó la orden */
    private String createdBy;
}
