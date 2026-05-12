package com.proyecto.TurboMechanics.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.RegisterMovementRequestDTO;
import com.proyecto.TurboMechanics.entity.MovementPay;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.MovementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovementController {

    private final MovementService movementService;
 
    /**
     * Registrar entrada o salida de dinero/inventario.
     * @param request dto para el registro del movimiento
     * @return retorna el registro del movimiento
     */
    @PostMapping
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    public ResponseEntity<MovementPay> register(@Valid @RequestBody RegisterMovementRequestDTO request) {
        try {
            MovementPay movementPay = movementService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(movementPay);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }    
    }
}
