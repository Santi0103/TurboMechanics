package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkshopRequestDTO;
import com.proyecto.TurboMechanics.dto.WorkshopResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.WorkshopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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
     * registra un nuevo taller. Solo ADMIN puede registrar talleres.
     * @param request objeto con los datos necesarios para crear el taller
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que registra el taller
     * @return datos del taller creado
     */
    @RequiresRole({RolEnum.ADMIN})
    @PostMapping
    public ResponseEntity<?> registerWorkshop(
            @Valid @RequestBody WorkshopRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            WorkshopResponseDTO response =
                    workshopService.registerWorkshop(request, extractUsername(httpRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * obtiene una lista de talleres activos, con opción de filtrar por ciudad.
     * @param ciudad filtro opcional por ciudad (si se proporciona, solo se retornan talleres 
     * de esa ciudad; si no, se retornan todos los talleres activos)
     * @return lista de talleres activos ordenados por nombre
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
     * obtiene una lista de talleres cercanos a una ubicación dada, dentro de un radio especificado.
      * @param lat latitud de la ubicación del cliente
      * @param lng longitud de la ubicación del cliente
      * @param radio radio de búsqueda en kilómetros (opcional, por defecto 50 km)
      * @return lista de talleres activos dentro del radio especificado, ordenados por distancia al cliente
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
     * obtiene los datos de un taller por su id.
     * @param id id del taller
     * @return datos del taller o error si no se encuentra el taller o si el taller está eliminado
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

    /**
     * actualiza los datos de un taller por su id. Solo ADMIN puede actualizar talleres.
     * @param id id del taller a modificar
     * @param request nuevos datos del taller
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que realiza la actualización
     * @return datos del taller actualizado o error si no se encuentra el taller, si el taller está 
     * eliminado o si se intenta modificar un taller eliminado
     */
    @RequiresRole({RolEnum.ADMIN})
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWorkshop(
            @PathVariable Long id,
            @Valid @RequestBody WorkshopRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            WorkshopResponseDTO response =
                    workshopService.updateWorkshop(id, request, extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * elimina un taller por su id mediante borrado lógico, marcándolo como inactivo y registrando 
     * fecha y usuario de eliminación.
     * @param id id del taller a eliminar
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que realiza la eliminación
     * @return mensaje de confirmación de eliminación o error si el taller ya fue eliminado
     */
    @RequiresRole({RolEnum.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteWorkshop(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            workshopService.deleteWorkshop(id, extractUsername(httpRequest));
            return ResponseEntity.ok(
                    new MessageResponseDTO("Taller eliminado correctamente"));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    private String extractUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? username.toString() : "sistema";
    }
}