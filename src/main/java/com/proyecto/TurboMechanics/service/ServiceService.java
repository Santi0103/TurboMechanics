package com.proyecto.TurboMechanics.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.PriceRequestDTO;
import com.proyecto.TurboMechanics.dto.ServiceRequestDTO;
import com.proyecto.TurboMechanics.dto.ServiceResponseDTO;
import com.proyecto.TurboMechanics.entity.ServiceEntity;
import com.proyecto.TurboMechanics.repository.ServiceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    /**
     * Registra un nuevo servicio en el sistema.
     *
     * @param request datos del servicio a registrar
     * @return servicio creado en formato DTO
     * @throws IllegalArgumentException si ya existe un servicio con el mismo nombre
     */
    @Transactional
    public ServiceResponseDTO registerService(ServiceRequestDTO request) {
        if (serviceRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException(
                    "Ya existe un servicio con el nombre: " + request.getName());
        }
        ServiceEntity service = ServiceEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .active(true)
                .build();
        return toResponse(serviceRepository.save(service));
    }

    /**
     * Actualiza el precio de un servicio existente.
     *
     * @param id identificador del servicio
     * @param request nuevo precio del servicio
     * @return servicio actualizado en formato DTO
     */
    @Transactional
    public ServiceResponseDTO updatePrice(Long id, PriceRequestDTO request) {
        ServiceEntity service = findOrThrow(id);
        service.setPrice(request.getPrice());
        return toResponse(serviceRepository.save(service));
    }

    /**
     * Obtiene el catálogo completo de servicios.
     *
     * @return lista de servicios en formato DTO
     */
    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> viewCatalog() {
        return serviceRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Busca un servicio por su identificador.
     *
     * @param id identificador del servicio
     * @return servicio encontrado en formato DTO
     */
    @Transactional(readOnly = true)
    public ServiceResponseDTO findById(Long id) {
        return toResponse(findOrThrow(id));
    }

    /**
     * Elimina un servicio por su identificador.
     *
     * @param id identificador del servicio
     */
    @Transactional
    public void deleteService(Long id) {
        if (!serviceRepository.existsById(id)) {
            throw new RuntimeException("Servicio no encontrado con id: " + id);
        }
        serviceRepository.deleteById(id);
    }

    /**
     * Cambia el estado activo/inactivo de un servicio.
     *
     * @param id identificador del servicio
     * @return servicio actualizado en formato DTO
     */
    @Transactional
    public ServiceResponseDTO changeStatus(Long id) {
        ServiceEntity service = findOrThrow(id);
        service.setActive(!service.getActive());
        return toResponse(serviceRepository.save(service));
    }

    /**
     * Busca un servicio por su identificador o lanza una excepción si no existe.
     *
     * @param id identificador del servicio
     * @return entidad del servicio encontrada
     */
    private ServiceEntity findOrThrow(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Servicio no encontrado con id: " + id));
    }

    /**
     * Convierte una entidad ServiceEntity a su representación DTO.
     *
     * @param s entidad del servicio
     * @return DTO del servicio
     */
    private ServiceResponseDTO toResponse(ServiceEntity s) {
        return ServiceResponseDTO.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .price(s.getPrice())
                .active(s.getActive())
                .build();
    }
}