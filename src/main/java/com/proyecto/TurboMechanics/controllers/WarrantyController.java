package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.CloseWarrantyRequestDTO;
import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.WarrantyRequestDTO;
import com.proyecto.TurboMechanics.dto.WarrantyResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.WarrantyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/garantias")
@RequiredArgsConstructor
public class WarrantyController {

    private final WarrantyService warrantyService;

    /**
     * registra una nueva garantía. Solo ADMIN puede registrar garantías.
     * @param request objeto con los datos necesarios para crear la garantía
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que registra la garantía
     * @return datos de la garantía creada
     */
    @RequiresRole({RolEnum.ADMIN})
    @PostMapping
    public ResponseEntity<?> registerWarranty(
            @Valid @RequestBody WarrantyRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            WarrantyResponseDTO response =
                    warrantyService.registerWarranty(request, extractUsername(httpRequest));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * obtiene una lista de garantías, con filtros opcionales por cliente, vehículo, servicio, repuesto o búsqueda general.
     * @param cliente filtro opcional por nombre del cliente
     * @param vehiculo filtro opcional por placa del vehículo
     * @param servicio filtro opcional por id del servicio asociado
     * @param repuesto filtro opcional por id del repuesto asociado
     * @param buscar filtro de búsqueda general que busca coincidencias en cliente, vehículo, servicio y repuesto
     * @return lista de garantías que coinciden con los filtros, ordenadas por fecha de creación
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping
    public ResponseEntity<?> getWarranties(
            @RequestParam(required = false) String cliente,
            @RequestParam(required = false) String vehiculo,
            @RequestParam(required = false) Long servicio,
            @RequestParam(required = false) Long repuesto,
            @RequestParam(required = false) String buscar) {
        try {
            List<WarrantyResponseDTO> result;

            if (buscar != null && !buscar.isBlank())
                result = warrantyService.searchWarranties(buscar);
            else if (cliente != null && !cliente.isBlank())
                result = warrantyService.getWarrantiesByClient(cliente);
            else if (vehiculo != null && !vehiculo.isBlank())
                result = warrantyService.getWarrantiesByVehicle(vehiculo);
            else if (servicio != null)
                result = warrantyService.getWarrantiesByService(servicio);
            else if (repuesto != null)
                result = warrantyService.getWarrantiesBySparePart(repuesto);
            else
                result = warrantyService.getAllWarranties();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error al consultar garantías"));
        }
    }

    /**
     * obtiene el detalle de una garantía por su id. Solo ADMIN puede consultar garantías por id.
     * @param id id de la garantía
     * @return datos de la garantía (incluyendo cliente, vehículo, servicio, repuesto, fechas, estado y observaciones)
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/{id}")
    public ResponseEntity<?> getWarrantyById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(warrantyService.getWarrantyById(id));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * actualiza los datos de una garantía por su id. Solo ADMIN puede actualizar garantías.
     * @param id id de la garantía a actualizar
     * @param request objeto con los campos a actualizar (serviceId, sparePartId, startDate, endDate, observations)
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que realiza la actualización
     * @return datos de la garantía actualizada
     */
    @RequiresRole({RolEnum.ADMIN})
    @PutMapping("/{id}")
    public ResponseEntity<?> updateWarranty(
            @PathVariable Long id,
            @Valid @RequestBody WarrantyRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            WarrantyResponseDTO response =
                    warrantyService.updateWarranty(id, request, extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * cierra una garantía por su id. Solo ADMIN y MECANICO pueden cerrar garantías.
     * @param id id de la garantía a cerrar
     * @param request objeto con el motivo de cierre y observaciones finales
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que cierra la garantía
     * @return datos de la garantía cerrada
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @PatchMapping("/{id}/cerrar")
    public ResponseEntity<?> closeWarranty(
            @PathVariable Long id,
            @Valid @RequestBody CloseWarrantyRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            WarrantyResponseDTO response =
                    warrantyService.closeWarranty(id, request, extractUsername(httpRequest));
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * genera un comprobante de garantía en formato PDF con toda la información relevante. Solo ADMIN.
     * @param id id de la garantía
     * @param httpRequest objeto de la solicitud HTTP para extraer el usuario que genera el comprobante
     * @return archivo PDF como byte array con encabezados para descarga
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/{id}/comprobante")
    public ResponseEntity<?> generateVoucher(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            byte[] pdf = warrantyService.generateWarrantyVoucher(id, extractUsername(httpRequest));
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"comprobante-garantia-" + id + ".pdf\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(pdf);
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