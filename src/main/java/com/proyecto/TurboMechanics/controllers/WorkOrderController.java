package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.CancelWorkOrderRequestDTO;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkOrderRequestDTO;
import com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkOrderUpdateRequestDTO;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.WorkOrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService ordenTrabajoService;

    /**
     * Endpoint para crear una nueva orden de trabajo. Solo accesible para usuarios
     * con rol MECANICO.
     * 
     * @param request el DTO con los datos de la orden a crear, validado
     *                automáticamente por Spring
     * @return 201 CREATED con la orden creada, o 400 BAD REQUEST si la validación
     *         falla o ocurre un error de negocio
     */
    @PostMapping
    @RequiresRole({ RolEnum.MECANICO })
    public ResponseEntity<WorkOrderResponseDTO> create(@Valid @RequestBody WorkOrderRequestDTO request) {
        try {
            WorkOrderResponseDTO response = ordenTrabajoService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Endpoint para listar todas las órdenes de trabajo. Solo accesible para
     * usuarios con rol MECANICO o ADMIN.
     * 
     * @return 200 OK con la lista de órdenes, o 500 INTERNAL SERVER ERROR si ocurre
     *         un error inesperado
     */
    @GetMapping
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<WorkOrderResponseDTO>> list() {
        try {
            List<WorkOrderResponseDTO> response = ordenTrabajoService.list();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint para obtener una orden de trabajo por su ID. Solo accesible para
     * usuarios con rol MECANICO o ADMIN.
     * 
     * @param id el ID de la orden a obtener
     * @return 200 OK con la orden encontrada, 404 NOT FOUND si no existe una orden
     *         con ese ID, o 500 INTERNAL SERVER ERROR si ocurre un error inesperado
     */
    @GetMapping("/{id}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<WorkOrderResponseDTO> getById(@PathVariable Long id) {
        try {
            WorkOrderResponseDTO response = ordenTrabajoService.getById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Endpoint para obtener una orden de trabajo por su número de orden. Solo
     * accesible para usuarios con rol MECANICO o ADMIN.
     * 
     * @param numberorder el número de orden a buscar
     * @return 200 OK con la orden encontrada, 404 NOT FOUND si no existe una orden
     *         con ese número, o 500 INTERNAL SERVER ERROR si ocurre un error
     *         inesperado
     */
    @GetMapping("/number/{numberorder}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<WorkOrderResponseDTO> getByNumber(@PathVariable String numberorder) {
        try {
            WorkOrderResponseDTO response = ordenTrabajoService.getByNumber(numberorder);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Endpoint para listar órdenes de trabajo por placa del vehículo. Solo
     * accesible para usuarios con rol MECANICO o ADMIN.
     * 
     * @param vehicleplate la placa del vehículo a buscar
     * @return 200 OK con la lista de órdenes encontradas, o 500 INTERNAL SERVER
     *         ERROR si ocurre un error inesperado
     */
    @GetMapping("/plate/{vehicleplate}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<WorkOrderResponseDTO>> listByPlate(@PathVariable String vehicleplate) {
        try {
            List<WorkOrderResponseDTO> response = ordenTrabajoService.listByPlate(vehicleplate);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint para listar órdenes de trabajo por identificación del cliente.
     * 
     * @param clientidentification
     * @return 200 OK con la lista de órdenes, o 500 INTERNAL SERVER ERROR si ocurre
     *         un error inesperado
     */
    @GetMapping("/client/{clientidentification}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<WorkOrderResponseDTO>> listByClient(@PathVariable String clientidentification) {
        try {
            List<WorkOrderResponseDTO> response = ordenTrabajoService.listByClient(clientidentification);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint para listar órdenes de trabajo por estado. Solo accesible para
     * usuarios con rol MECANICO o ADMIN.
     * 
     * @param stateorder el estado a filtrar (RECIBIDO, EN_DIAGNOSTICO,
     *                   EN_REPARACION, LISTO, ENTREGADO, CANCELADO)
     * @return 200 OK con la lista de órdenes que coinciden con el estado, o 500
     *         INTERNAL SERVER ERROR si ocurre un error inesperado
     */
    @GetMapping("/state/{stateorder}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<WorkOrderResponseDTO>> listByState(@PathVariable WorkOrder.StateOrder stateorder) {
        try {
            List<WorkOrderResponseDTO> response = ordenTrabajoService.listByState(stateorder);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint para actualizar una orden de trabajo existente. Solo accesible para
     * usuarios con rol MECANICO o ADMIN.
     * 
     * @param id      el ID de la orden a actualizar
     * @param request el DTO con los datos a actualizar, validado automáticamente
     *                por Spring
     * @return 200 OK con la orden actualizada
     */
    @PutMapping("/{id}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody WorkOrderUpdateRequestDTO request) {
        try {
            WorkOrderResponseDTO response = ordenTrabajoService.update(id, request);
            return ResponseEntity.ok()
                    .body(new java.util.LinkedHashMap<String, Object>() {
                        {
                            put("message", "La orden de trabajo ha sido actualizada correctamente.");
                            put("order", response);
                        }
                    });
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Endpoint para cancelar una orden de trabajo. Solo accesible para usuarios con
     * rol ADMIN.
     * 
     * @param id      el ID de la orden a cancelar
     * @param request el DTO con el motivo de cancelación, validado automáticamente
     *                por Spring
     * @return 200 OK con la orden cancelada
     */
    @PatchMapping("/{id}/cancel")
    @RequiresRole({ RolEnum.ADMIN })
    public ResponseEntity<?> cancel(@PathVariable Long id, @Valid @RequestBody CancelWorkOrderRequestDTO request) {
        try {
            WorkOrderResponseDTO response = ordenTrabajoService.cancel(id, request);
            return ResponseEntity.ok()
                    .body(new java.util.LinkedHashMap<String, Object>() {
                        {
                            put("message", "La orden de trabajo ha sido cancelada correctamente.");
                            put("order", response);
                        }
                    });
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Endpoint para cambiar el estado de una orden de trabajo.
     * 
     * @param id   el ID de la orden
     * @param body mapa con la clave "state" y el nuevo estado
     * @return 200 OK con la orden actualizada
     */
    @PatchMapping("/{id}/state")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<?> changeState(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        try {
            WorkOrder.StateOrder newState = WorkOrder.StateOrder.valueOf(body.get("state"));
            WorkOrderResponseDTO response = ordenTrabajoService.changeState(id, newState);
            return ResponseEntity.ok()
                    .body(new java.util.LinkedHashMap<String, Object>() {
                        {
                            put("message", "Estado actualizado correctamente.");
                            put("order", response);
                        }
                    });
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }
}
