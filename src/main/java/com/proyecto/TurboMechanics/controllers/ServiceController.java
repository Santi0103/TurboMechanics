package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.PriceRequestDTO;
import com.proyecto.TurboMechanics.dto.ServiceHistoryCheckResponseDTO;
import com.proyecto.TurboMechanics.dto.ServiceRequestDTO;
import com.proyecto.TurboMechanics.dto.ServiceResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.ServiceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@RestController
@RequestMapping("/admin/catalogo")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final ServiceService serviceService;

    /**
     * registrar servicio en el sistema
     * 
     * @param request ServiceRequestDTO dto que pide los datos
     * @return retorna el registro del servicio creado
     */

    @RequiresRole({ RolEnum.ADMIN })
    @PostMapping
    public ResponseEntity<ServiceResponseDTO> registerService(@Valid @RequestBody ServiceRequestDTO request) {
        try {
            ServiceResponseDTO response = serviceService.registerService(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(null);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Actualiza el precio del servicio
     * 
     * @param id      id del servicio a actualizar el precio
     * @param request PriceRequestDTO dto que pide los datos
     * @return retorna la actualizacion del precio
     */
    @RequiresRole({ RolEnum.ADMIN })
    @PatchMapping("/{id}/price")
    public ResponseEntity<ServiceResponseDTO> updatePrice(@PathVariable Long id,
            @Valid @RequestBody PriceRequestDTO request) {
        try {
            ServiceResponseDTO response = serviceService.updatePrice(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Muestra todo el catalogo de servicios
     * 
     * @return retorna todo el catalogo de servicio
     */
    @RequiresRole({ RolEnum.ADMIN })
    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> viewCatalog() {
        try {
            List<ServiceResponseDTO> response = serviceService.viewCatalog();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Busca el servicio por id
     * 
     * @param id id del servicio a buscar
     * @return retorna el servicio que busco
     */
    @RequiresRole({ RolEnum.ADMIN })
    @GetMapping("/{id}")
    public ResponseEntity<ServiceResponseDTO> findById(@PathVariable Long id) {
        try {
            ServiceResponseDTO response = serviceService.findById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Verifica si el servicio tiene garantías asociadas antes de eliminarlo,
     * para mostrar una advertencia en el frontend. No bloquea la eliminación,
     * solo informa.
     *
     * @param id id del servicio
     * @return ServiceHistoryCheckResponseDTO con el detalle del historial
     */
    @RequiresRole({ RolEnum.ADMIN })
    @GetMapping("/{id}/historial-check")
    public ResponseEntity<ServiceHistoryCheckResponseDTO> checkHistory(@PathVariable Long id) {
        return ResponseEntity.ok(serviceService.checkHistory(id));
    }

    /**
     * Elimina el servicio por id
     * 
     * @param id id del servicio para eliminar
     * @return retorna la eliminacion del servicio
     */
    @RequiresRole({ RolEnum.ADMIN })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteService(@PathVariable Long id) {
        try {
            serviceService.deleteService(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Error al eliminar servicio {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", e.getMessage() != null ? e.getMessage() : "Error al eliminar el servicio"));
        }
    }

    /**
     * Cambiar el estado del servicio
     * 
     * @param id id del servicio
     * @return retorna el estado del servicio nuevo
     */
    @RequiresRole({ RolEnum.ADMIN })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ServiceResponseDTO> changeStatus(@PathVariable Long id) {
        try {
            ServiceResponseDTO response = serviceService.changeStatus(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Catalogo publico de servicios activos para la pagina principal
     * 
     * @return retorna los servicios activos
     */
    @GetMapping("/public/servicios")
    public ResponseEntity<List<ServiceResponseDTO>> publicCatalog() {
        try {
            List<ServiceResponseDTO> response = serviceService.viewCatalog()
                    .stream()
                    .filter(s -> Boolean.TRUE.equals(s.getActive()))
                    .collect(java.util.stream.Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}