package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.SentEstimateRequestDTO;
import com.proyecto.TurboMechanics.entity.Estimate;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.EstimateService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/presupuestos")
@RequiredArgsConstructor
public class EstimateController {

    private final EstimateService estimateService;
 
   /**
    * Enviar el presupuesto del cliente
    * @param request dto para enviar el presupuesto
    * @return envia el presupuesto del cliente
    */
    @PostMapping
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<Estimate> sent(@Valid @RequestBody SentEstimateRequestDTO request) {
        try {
            Estimate estimate = estimateService.sendtEstimate(request);
            return ResponseEntity.status(HttpStatus.OK).body(estimate);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
 
    /**
     * respuesta de aprobacion o no del cliente
     * @param id id de la respuesta
     * @param approved aprobado o no del cliente
     * @return retorna la respuesta del cliente
     */
    @PatchMapping("/{id}/respuesta")
    public ResponseEntity<Estimate> response(@PathVariable Long id, @RequestParam boolean approved) {
        try {
            Estimate estimate = estimateService.response(id, approved);
            return ResponseEntity.status(HttpStatus.OK).body(estimate);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
 
    /**
     * consultar presupuesto del cliente
     * @param identification identificacion del cliente
     * @param plate placa del vehiculo
     * @return retorna la cunsulta del presupuesto
     */
    @GetMapping
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<List<Estimate>> listar(@RequestParam Integer identification, @RequestParam(required = false) String plate) {
        try {
            List<Estimate> estimate = estimateService.listByClient(identification, plate);
            return ResponseEntity.status(HttpStatus.OK).body(estimate);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
