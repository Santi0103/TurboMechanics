package com.proyecto.TurboMechanics.dto;

import com.proyecto.TurboMechanics.entity.WorkOrder;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class WorkOrderRequestDTO {

    /** El nombre del cliente (debe ser texto, no solo números) */
    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 150)
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El nombre del cliente no puede ser solo números o símbolos")
    private String clientname;

    /** La identificación del cliente (solo números) */
    @NotBlank(message = "La identificación del cliente es obligatoria")
    @Pattern(regexp = "^[0-9]{5,20}$", message = "La identificación debe contener únicamente números (5 a 20 dígitos)")
    private String clientidentification;

    /** El número de teléfono del cliente */
    @NotBlank(message = "El teléfono del cliente es obligatorio")
    @Pattern(regexp = "^[+]?[0-9\\s\\-]{7,20}$", message = "Formato de teléfono inválido")
    private String clientphone;

    /** La placa del vehículo */
    @NotBlank(message = "La placa del vehículo es obligatoria")
    @Size(max = 10)
    private String vehicleplate;

    /** La marca del vehículo (debe ser texto, no solo números) */
    @NotBlank(message = "La marca del vehículo es obligatoria")
    @Size(max = 50)
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "La marca no puede ser solo números o símbolos")
    private String vehiclebrand;

    /** El modelo del vehículo (debe contener letras; algunos modelos llevan números, ej. "Civic 2.0") */
    @NotBlank(message = "El modelo del vehículo es obligatorio")
    @Size(max = 50)
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "El modelo no puede ser solo números o símbolos")
    private String vehiclemodel;

    /** El año del vehículo */
    @NotNull(message = "El año del vehículo es obligatorio")
    @Min(value = 1900, message = "Año inválido")
    @Max(value = 2100, message = "Año inválido")
    private Integer vehicleyear;

    /** El color del vehículo */
    @Size(max = 30)
    private String vehiclecolor;

    /** Las fallas reportadas (debe ser texto explicativo, no solo números) */
    @NotBlank(message = "Las fallas reportadas son obligatorias")
    @Size(min = 5, max = 1000, message = "Las fallas reportadas deben tener entre 5 y 1000 caracteres")
    @Pattern(regexp = "^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "Las fallas reportadas deben ser un texto explicativo, no solo números o símbolos")
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

    /** Las observaciones sobre los accesorios (opcional; si se informa, debe ser texto) */
    @Pattern(regexp = "^$|^(?=.*[A-Za-zÁÉÍÓÚÑáéíóúñ]).+$", message = "Las observaciones deben ser un texto, no solo números o símbolos")
    private String accessoriesobservations;

    /** La prioridad de la orden */
    private WorkOrder.Priority priority;

    /** El usuario que creó la orden */
    private String createdBy;
}