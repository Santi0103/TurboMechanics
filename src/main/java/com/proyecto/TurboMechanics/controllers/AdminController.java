package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.UserRequestDTO;
import com.proyecto.TurboMechanics.dto.UserResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.AdminService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
 
    /**
     * Obtiene todos los clientes
     * @return retorna todos los clientes que hay registrados
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping("/users")
    public ResponseEntity<List<UserResponseDTO>> getAllClients() {
        try {
            List<UserResponseDTO> clients = adminService.getAllClients();
            return ResponseEntity.ok(clients);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
 
    /**
     * Busca el cliente por su identificacion
     * @param identification identificacion del cliente
     * @return retorna el cliente en base a su identificacion
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/users/{identification}")
    public ResponseEntity<UserResponseDTO> getClientByIdentification(@PathVariable Integer identification) {
        try {
            UserResponseDTO client = adminService.getClientByIdentification(identification);
            return ResponseEntity.ok(client);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    
    /**
     * Consultar historial de servicios por cliente
     * @param identification identification del cliente
     * @return retorna el historial de servicio en base a al identificacion
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping("/users/{identification}/history")
    public ResponseEntity<List<WorkOrderResponseDTO>> getServiceHistoryByClient(
            @PathVariable Integer identification) {
        try {
            List<WorkOrderResponseDTO> history = adminService.getServiceHistoryByClient(identification);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
    /**
     * Actualizar la informacion del cliente
     * @param identification identificacion del cliente
     * @param request UserRequestDTO par ala actualizacion de los datos
     * @return retorna los datos ya actualizados
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PutMapping("/users/{identification}")
    public ResponseEntity<UserResponseDTO> updateClient(@PathVariable Integer identification, @Valid @RequestBody UserRequestDTO request) {
        try {
            UserResponseDTO updated = adminService.updateClient(identification, request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Elimina al cliente permanentemente
     * @param identification identificacion del cliente
     * @return  MessageResponseDTO eliminacr cliente
     */
    @RequiresRole({RolEnum.ADMIN})
    @DeleteMapping("/users/{identification}")
    public ResponseEntity<MessageResponseDTO> deleteClient(@PathVariable Integer identification) {
        try {
            adminService.deleteClient(identification);
            MessageResponseDTO response = new MessageResponseDTO();
            response.setMessage("Cliente Eliminado Correctamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageResponseDTO error = new MessageResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

}
