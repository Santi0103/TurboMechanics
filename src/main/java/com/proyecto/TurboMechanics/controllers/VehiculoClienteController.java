package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.VehiculoClienteRequestDTO;
import com.proyecto.TurboMechanics.dto.VehiculoClienteResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.VehiculoClienteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/cliente/vehiculos")
@RequiredArgsConstructor
public class VehiculoClienteController {

    private final VehiculoClienteService vehiculoClienteService;

    /**
     * Registra un nuevo vehiculo para el cliente autenticado
     * @param request HttpServletRequest para extraer el userId del JWT
     * @param body VehiculoClienteRequestDTO con los datos del vehiculo
     * @return retorna el vehiculo registrado
     */
    @PostMapping
    public ResponseEntity<VehiculoClienteResponseDTO> registrar(
            HttpServletRequest request,
            @Valid @RequestBody VehiculoClienteRequestDTO body) {
        try {
            Long usuarioId = (Long) request.getAttribute("userId");
            VehiculoClienteResponseDTO response = vehiculoClienteService.register(usuarioId, body);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Lista todos los vehiculos del cliente autenticado
     * @param request HttpServletRequest para extraer el userId del JWT
     * @return retorna la lista de vehiculos del cliente
     */
    @GetMapping
    public ResponseEntity<List<VehiculoClienteResponseDTO>> listarMisVehiculos(HttpServletRequest request) {
        try {
            Long usuarioId = (Long) request.getAttribute("userId");
            return ResponseEntity.ok(vehiculoClienteService.listMyVehicles(usuarioId));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Obtiene un vehiculo del cliente por su id
     * @param id id del vehiculo
     * @param request HttpServletRequest para extraer el userId del JWT
     * @return retorna el vehiculo encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<VehiculoClienteResponseDTO> obtener(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long usuarioId = (Long) request.getAttribute("userId");
            return ResponseEntity.ok(vehiculoClienteService.getVehicle(id, usuarioId));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Actualiza los datos de un vehiculo del cliente
     * @param id id del vehiculo a actualizar
     * @param request HttpServletRequest para extraer el userId del JWT
     * @param body VehiculoClienteRequestDTO con los nuevos datos
     * @return retorna el vehiculo actualizado
     */
    @PutMapping("/{id}")
    public ResponseEntity<VehiculoClienteResponseDTO> actualizar(
            @PathVariable Long id,
            HttpServletRequest request,
            @Valid @RequestBody VehiculoClienteRequestDTO body) {
        try {
            Long usuarioId = (Long) request.getAttribute("userId");
            return ResponseEntity.ok(vehiculoClienteService.update(id, usuarioId, body));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Elimina un vehiculo del cliente
     * @param id id del vehiculo a eliminar
     * @param request HttpServletRequest para extraer el userId del JWT
     * @return retorna un mensaje de confirmacion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> eliminar(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long usuarioId = (Long) request.getAttribute("userId");
            vehiculoClienteService.delete(id, usuarioId);
            MessageResponseDTO response = new MessageResponseDTO();
            response.setMessage("Vehículo eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            MessageResponseDTO error = new MessageResponseDTO();
            error.setMessage(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    
    /**
     * Lista todos los vehiculos de todos los clientes (solo admin y mecanico)
     * @return retorna la lista completa de vehiculos
     */
    @GetMapping("/admin/all")
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<List<VehiculoClienteResponseDTO>> listAll() {
        try {
            return ResponseEntity.ok(vehiculoClienteService.listAll());
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
 
    /**
     * Lista los vehiculos de un cliente especifico por su usuarioId (solo admin y mecanico)
     * @param usuarioId id del usuario cliente
     * @return retorna la lista de vehiculos del cliente
     */
    @GetMapping("/admin/usuario/{usuarioId}")
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<List<VehiculoClienteResponseDTO>> listByUsuario(@PathVariable Long usuarioId) {
        try {
            return ResponseEntity.ok(vehiculoClienteService.listByUsuario(usuarioId));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}