package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.entity.PayMethod;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.PayMethodService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/metodos-pago")
@RequiredArgsConstructor
public class PayMethodController {

    private final PayMethodService payMethodService;
    
    /**
     * Lista todos los metodos de pago
     * @return retorna todos lo metodos de pago
     */
    @GetMapping
    public ResponseEntity<List<PayMethod>> list() {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
 
    /**
     * Crea el metodo de pago
     * @param method entidad para crear el metodo de pago
     * @return retorna el metodo de pago creado
     */
    @PostMapping
    @RequiresRole({RolEnum.ADMIN})
    public ResponseEntity<PayMethod> create(@Valid @RequestBody PayMethod method) {
        try {
            PayMethod payMethod = payMethodService.create(method);
            return ResponseEntity.status(HttpStatus.CREATED).body(payMethod);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
 
    /**
     * Actualiza el metodo de pago
     * @param id id del metodo de pago
     * @param data entidad que pide los datos para actualizarlos
     * @return retorna el metodo de pago actualizado
     */
    @PutMapping("/{id}")
    @RequiresRole({RolEnum.ADMIN})
    public ResponseEntity<PayMethod> update(@PathVariable Long id,@Valid @RequestBody PayMethod data) {
        try {
            PayMethod payMethod = payMethodService.update(id, data);
            return ResponseEntity.ok(payMethod);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
