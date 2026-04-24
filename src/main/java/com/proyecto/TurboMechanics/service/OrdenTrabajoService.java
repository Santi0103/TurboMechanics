package com.proyecto.TurboMechanics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.proyecto.TurboMechanics.dto.OrdenTrabajoRequestDTO;
import com.proyecto.TurboMechanics.dto.OrdenTrabajoResponseDTO;
import com.proyecto.TurboMechanics.entity.OrdenTrabajo;
import com.proyecto.TurboMechanics.repository.OrdenTrabajoRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class OrdenTrabajoService {
    
    private final OrdenTrabajoRepository ordenTrabajoRepository;

    @Transactional
    public OrdenTrabajoResponseDTO crear(@Valid OrdenTrabajoRequestDTO request) {

        OrdenTrabajo orden = new OrdenTrabajo();
        orden.setClienteNombre(request.getClienteNombre());
        orden.setClienteIdentificacion(request.getClienteIdentificacion());
        orden.setClienteTelefono(request.getClienteTelefono());
        orden.setVehiculoPlaca(request.getVehiculoPlaca().toUpperCase().trim());
        orden.setVehiculoMarca(request.getVehiculoMarca());
        orden.setVehiculoModelo(request.getVehiculoModelo());
        orden.setVehiculoAnio(request.getVehiculoAnio());
        orden.setVehiculoColor(request.getVehiculoColor());
        orden.setFallasReportadas(request.getFallasReportadas());
        orden.setFechaEntregaEstimada(request.getFechaEntregaEstimada());
        orden.setNivelCombustible(request.getNivelCombustible());
        orden.setEstadoRayones(request.getEstadoRayones());
        orden.setEstadoAbolladuras(request.getEstadoAbolladuras());
        orden.setAccesoriosObservaciones(request.getAccesoriosObservaciones());
        orden.setPrioridad(request.getPrioridad() != null ? request.getPrioridad() : OrdenTrabajo.Prioridad.NORMAL);
        orden.setCreadoPor(request.getCreadoPor());
        orden.setNumeroOrden(generarNumeroOrden());

        OrdenTrabajo guardada = ordenTrabajoRepository.save(orden);
        return toResponse(guardada);
    }

    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponseDTO> listar() {
        return ordenTrabajoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdenTrabajoResponseDTO obtenerPorId(Long id) {
        OrdenTrabajo orden = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con id: " + id));
        return toResponse(orden);
    }

    @Transactional(readOnly = true)
    public OrdenTrabajoResponseDTO obtenerPorNumero(String numeroOrden) {
        OrdenTrabajo orden = ordenTrabajoRepository.findByNumeroOrden(numeroOrden)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + numeroOrden));
        return toResponse(orden);
    }

    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponseDTO> buscarPorPlaca(String placa) {
        return ordenTrabajoRepository.findByVehiculoPlacaIgnoreCase(placa)
                .stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenTrabajoResponseDTO> buscarPorCliente(String identificacion) {
        return ordenTrabajoRepository.findByClienteIdentificacion(identificacion)
                .stream().map(this::toResponse).toList();
    }

    // Genera número único: OT-YYYY-NNNN
    private String generarNumeroOrden() {
        int anio = LocalDateTime.now().getYear();
        long cantidad = ordenTrabajoRepository.countByAnio(anio) + 1;
        String numero = String.format("OT-%d-%04d", anio, cantidad);
        while (ordenTrabajoRepository.existsByNumeroOrden(numero)) {
            cantidad++;
            numero = String.format("OT-%d-%04d", anio, cantidad);
        }
        return numero;
    }

    private OrdenTrabajoResponseDTO toResponse(OrdenTrabajo o) {
        OrdenTrabajoResponseDTO dto = new OrdenTrabajoResponseDTO();
        dto.setId(o.getId());
        dto.setNumeroOrden(o.getNumeroOrden());
        dto.setClienteNombre(o.getClienteNombre());
        dto.setClienteIdentificacion(o.getClienteIdentificacion());
        dto.setClienteTelefono(o.getClienteTelefono());
        dto.setVehiculoPlaca(o.getVehiculoPlaca());
        dto.setVehiculoMarca(o.getVehiculoMarca());
        dto.setVehiculoModelo(o.getVehiculoModelo());
        dto.setVehiculoAnio(o.getVehiculoAnio());
        dto.setVehiculoColor(o.getVehiculoColor());
        dto.setFallasReportadas(o.getFallasReportadas());
        dto.setFechaIngreso(o.getFechaIngreso());
        dto.setFechaEntregaEstimada(o.getFechaEntregaEstimada());
        dto.setNivelCombustible(o.getNivelCombustible());
        dto.setEstadoRayones(o.getEstadoRayones());
        dto.setEstadoAbolladuras(o.getEstadoAbolladuras());
        dto.setAccesoriosObservaciones(o.getAccesoriosObservaciones());
        dto.setEstadoOrden(o.getEstadoOrden());
        dto.setPrioridad(o.getPrioridad());
        dto.setCreadoPor(o.getCreadoPor());
        dto.setFechaCreacion(o.getFechaCreacion());
        return dto;
    }
}
