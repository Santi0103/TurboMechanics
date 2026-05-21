package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.WarrantyResponseDTO;
import com.proyecto.TurboMechanics.dto.WarrantyValidationResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.WarrantyHistoryAndValidationService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/garantias")
@RequiredArgsConstructor
public class WarrantyHistoryController {

    private final WarrantyHistoryAndValidationService warrantyHistoryService;

    /**
     * Obtiene el historial de garantías asociadas a un cliente o vehículo, o realiza una búsqueda general por texto.
      * Se pueden usar los filtros cliente, vehículo o buscar (texto libre) para obtener resultados específicos.
      * @param cliente filtro opcional por nombre del cliente
      * @param vehiculo filtro opcional por placa del vehículo
      * @param buscar filtro de búsqueda general que busca coincidencias en cliente, vehículo, servicio y repuesto
      * @return lista de garantías que coinciden con los filtros, ordenadas por fecha de creación
      */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/historial")
    public ResponseEntity<?> getHistory(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String vehiculo,
            @RequestParam(required = false) String buscar) {
        try {
            List<WarrantyResponseDTO> result;

            if (buscar != null && !buscar.isBlank())
                result = warrantyHistoryService.searchHistory(buscar);
            else if (cliente != null && !cliente.isBlank())
                result = warrantyHistoryService.getHistoryByClient(cliente);
            else if (vehiculo != null && !vehiculo.isBlank())
                result = warrantyHistoryService.getHistoryByVehicle(vehiculo);
            else
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new MessageResponseDTO(
                                "Debe indicar al menos un filtro: cliente, vehiculo o buscar."));

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error al consultar historial de garantías"));
        }
    }

    /**
     * validar una garantía por su id, lo que implica verificar si aún está vigente y si cumple con las 
     * condiciones para ser válida.
     * @param id id de la garantía a validar
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que realiza la validación
     * @return resultado de la validación, indicando si la garantía es válida, vencida o cerrada, junto 
     * con detalles de la validación realizada
     */
    @RequiresRole({RolEnum.ADMIN})
    @PostMapping("/{id}/validar")
    public ResponseEntity<?> validateWarranty(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            WarrantyValidationResponseDTO response =
                    warrantyHistoryService.validateWarranty(id, extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Obtiene el historial de validaciones realizadas a una garantía, incluyendo fechas, 
     * resultados y usuarios que realizaron las validaciones.
     * @param id id de la garantía
     * @return lista de validaciones realizadas a la garantía, ordenadas por fecha de validación
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/{id}/validaciones")
    public ResponseEntity<?> getValidationHistory(@PathVariable Long id) {
        try {
            List<WarrantyValidationResponseDTO> history =
                    warrantyHistoryService.getValidationHistory(id);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    private String extractUsername(HttpServletRequest request) {
        Object username = request.getAttribute("username");
        return username != null ? username.toString() : "sistema";
    }
}