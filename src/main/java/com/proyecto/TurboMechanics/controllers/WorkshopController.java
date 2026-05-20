package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkshopResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.WorkshopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/talleres")
@RequiredArgsConstructor
public class WorkshopController {

    private final WorkshopService workshopService;

    /**
     * Obtiene una lista de talleres activos, opcionalmente filtrados por ciudad.
     * @param ciudad filtro opcional por ciudad (case-insensitive)
     * @return lista de talleres activos que coinciden con el filtro, ordenados por nombre
     */
    @RequiresRole({RolEnum.CLIENTE, RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping
    public ResponseEntity<?> getActiveWorkshops(
            @RequestParam(required = false) String ciudad) {
        try {
            List<WorkshopResponseDTO> workshops = workshopService.getActiveWorkshops(ciudad);
            return ResponseEntity.ok(workshops);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error al consultar talleres"));
        }
    }

    /**
     * Obtiene talleres activos cercanos a la ubicación del cliente, ordenados por distancia.
     * Parámetros obligatorios: latitud y longitud del cliente.
     * @param lat latitud del cliente
     * @param lng longitud del cliente
     * @param radio radio de búsqueda en km
     * @return lista de talleres cercanos
     */
    @RequiresRole({RolEnum.CLIENTE, RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping("/cercanos")
    public ResponseEntity<?> getWorkshopsNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) Double radio) {
        try {
            List<WorkshopResponseDTO> workshops =
                    workshopService.getWorkshopsNearby(lat, lng, radio);
            return ResponseEntity.ok(workshops);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * obten el detalle de un taller por su id. Solo muestra talleres activos.
     * @param id id del taller
     * @return datos del taller
     */
    @RequiresRole({RolEnum.CLIENTE, RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping("/{id}")
    public ResponseEntity<?> getWorkshopById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(workshopService.getWorkshopById(id));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }
}