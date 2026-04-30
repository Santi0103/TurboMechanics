package com.proyecto.TurboMechanics.controllers;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.TurboMechanics.dto.AssociateOwnerRequestDTO;
import com.proyecto.TurboMechanics.dto.VehicleRequestDTO;
import com.proyecto.TurboMechanics.dto.VehicleResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    
    private final VehicleService vehicleService;

    /**
     * Registra un vehículo y lo asocia al cliente propietario indicado.
     * @param request DTO con los datos del vehículo y el ID del propietario
     * @return 201 CREATED con el vehículo registrado, o 400 BAD REQUEST si la validación falla o ocurre un error de negocio
     */
    @PostMapping
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<VehicleResponseDTO> create(@Valid @RequestBody VehicleRequestDTO request) {
        try {
            VehicleResponseDTO response = vehicleService.create(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Lista todos los vehículos registrados.
     * @return 200 OK con la lista de vehículos, o 500 INTERNAL SERVER ERROR si ocurre un error inesperado
     */
    @GetMapping
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<VehicleResponseDTO>> list() {
        try {
            return ResponseEntity.ok(vehicleService.list());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtiene un vehículo por su ID.
     * @param id ID del vehículo a buscar
     * @return 200 OK con el vehículo encontrado, o 404 NOT FOUND si no existe un vehículo con ese ID
     */
    @GetMapping("/{id}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<VehicleResponseDTO> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(vehicleService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Obtiene un vehículo por su placa.
     * @param plate placa del vehículo a buscar
     * @return 200 OK con el vehículo encontrado, o 404 NOT FOUND si no existe un vehículo con esa placa
     */
    @GetMapping("/plate/{plate}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<VehicleResponseDTO> getByPlate(@PathVariable String plate) {
        try {
            return ResponseEntity.ok(vehicleService.getByPlate(plate));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * lista todos los vehículos asociados a un cliente por su ID de usuario.
     * @param ownerId ID del usuario propietario a buscar
     * @return 200 OK con la lista de vehículos del cliente, o 404 NOT FOUND si no existe un cliente con ese ID o no tiene vehículos asociados
     */
    @GetMapping("/owner/{ownerId}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<VehicleResponseDTO>> listByOwner(@PathVariable Long ownerId) {
        try {
            return ResponseEntity.ok(vehicleService.listByOwner(ownerId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * lista todos los vehículos asociados a un cliente por su identificación (cédula).
     * @param identification cédula del cliente a buscar
     * @return 200 OK con la lista de vehículos del cliente, o 500 INTERNAL SERVER ERROR si ocurre un error inesperado
     */
    @GetMapping("/owner/identification/{identification}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<VehicleResponseDTO>> listByOwnerIdentification(@PathVariable Integer identification) {
        try {
            return ResponseEntity.ok(vehicleService.listByOwnerIdentification(identification));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * actualiza o cambia el cliente asociado a un vehículo.
     * @param id ID del vehículo a actualizar
     * @param request AssociateOwnerRequestDTO con el ID del nuevo propietario
     * @return 200 OK con el vehículo actualizado, o 404 NOT FOUND si no se encuentra el vehículo o el nuevo propietario
     */
    @PutMapping("/{id}/owner")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<VehicleResponseDTO> updateOwner(
            @PathVariable Long id,
            @Valid @RequestBody AssociateOwnerRequestDTO request) {
        try {
            return ResponseEntity.ok(vehicleService.updateOwner(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * lista el historial de órdenes de trabajo asociadas a un vehículo por su placa.
     * @param plate placa del vehículo
     * @return 200 OK con el historial de servicios del vehículo, o 404 NOT FOUND si no se encuentra el vehículo o no tiene historial de servicios
     */
    @GetMapping("/plate/{plate}/history")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<WorkOrderResponseDTO>> getVehicleHistory(@PathVariable String plate) {
        try {
            return ResponseEntity.ok(vehicleService.getVehicleHistory(plate));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * historial de órdenes de trabajo asociadas a un cliente por su identificación (cédula).
     * @param identification cédula del cliente
     * @return 200 OK con el historial de servicios del cliente, o 500 INTERNAL SERVER ERROR si ocurre un error inesperado
      */
    @GetMapping("/owner/identification/{identification}/history")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<WorkOrderResponseDTO>> getClientHistory(@PathVariable String identification) {
        try {
            return ResponseEntity.ok(vehicleService.getClientHistory(identification));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
