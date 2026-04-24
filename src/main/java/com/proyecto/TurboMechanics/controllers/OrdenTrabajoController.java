package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.OrdenTrabajoRequestDTO;
import com.proyecto.TurboMechanics.dto.OrdenTrabajoResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.OrdenTrabajoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ordenes")
@RequiredArgsConstructor
public class OrdenTrabajoController {
    
    private final OrdenTrabajoService ordenTrabajoService;

    @PostMapping
    @RequiresRole({ RolEnum.MECANICO })
    public ResponseEntity<OrdenTrabajoResponseDTO> crear(@Valid @RequestBody OrdenTrabajoRequestDTO request) {
        try {
            OrdenTrabajoResponseDTO response = ordenTrabajoService.crear(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> listar() {
        try {
            List<OrdenTrabajoResponseDTO> response = ordenTrabajoService.listar();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{id}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<OrdenTrabajoResponseDTO> obtenerPorId(@PathVariable Long id) {
        try {
            OrdenTrabajoResponseDTO response = ordenTrabajoService.obtenerPorId(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/numero/{numeroOrden}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<OrdenTrabajoResponseDTO> obtenerPorNumero(@PathVariable String numeroOrden) {
        try {
            OrdenTrabajoResponseDTO response = ordenTrabajoService.obtenerPorNumero(numeroOrden);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/placa/{placa}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> buscarPorPlaca(@PathVariable String placa) {
        try {
            List<OrdenTrabajoResponseDTO> response = ordenTrabajoService.buscarPorPlaca(placa);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/cliente/{identificacion}")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<List<OrdenTrabajoResponseDTO>> buscarPorCliente(@PathVariable String identificacion) {
        try {
            List<OrdenTrabajoResponseDTO> response = ordenTrabajoService.buscarPorCliente(identificacion);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
