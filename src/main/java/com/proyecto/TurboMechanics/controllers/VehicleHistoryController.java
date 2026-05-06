package com.proyecto.TurboMechanics.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.VehicleHistoryResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.VehicleHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/history")
@RequiredArgsConstructor
public class VehicleHistoryController {

    private final VehicleHistoryService vehicleHistoryService;

    /**
     * busca el historial de un vehículo por PLACA.
     * @param plate placa del vehículo a consultar
     * @return 200 OK con el historial (o mensaje vacío si no hay registros — criterio 5)
     */
    @GetMapping("/vehicle/plate/{plate}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<VehicleHistoryResponseDTO> getHistoryByPlate(
            @PathVariable String plate) {
        try {
            VehicleHistoryResponseDTO response = vehicleHistoryService.getHistoryByPlate(plate);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * busca el historial de un vehículo por IDENTIFICACIÓN DEL CLIENTE.
     * @param clientIdentification identificación del cliente propietario del vehículo
     * @return 200 OK con el historial (o mensaje vacío si no hay registros — criterio 5)
     */
    @GetMapping("/vehicle/client/{clientIdentification}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<VehicleHistoryResponseDTO> getHistoryByClient(
            @PathVariable String clientIdentification) {
        try {
            VehicleHistoryResponseDTO response =
                    vehicleHistoryService.getHistoryByClientIdentification(clientIdentification);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
