package com.proyecto.TurboMechanics.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.proyecto.TurboMechanics.dto.AssociateOwnerRequestDTO;
import com.proyecto.TurboMechanics.dto.VehicleRequestDTO;
import com.proyecto.TurboMechanics.dto.VehicleResponseDTO;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.entity.Vehicle;
import com.proyecto.TurboMechanics.repository.UsersRepository;
import com.proyecto.TurboMechanics.repository.VehicleRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final UsersRepository usersRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * Registra un vehículo y lo asocia al cliente propietario indicado.
     * Criterios 1, 2, 3: selecciona cliente registrado, vehículo y establece la relación.
     * @param request DTO con los datos del vehículo y el ID del propietario
     * @return VehicleResponseDTO con los datos del vehículo y su propietario
     */
    @Transactional
    public VehicleResponseDTO create(@Valid VehicleRequestDTO request) {
        if (vehicleRepository.existsByPlateIgnoreCase(request.getPlate())) {
            throw new RuntimeException("Ya existe un vehículo registrado con la placa: " + request.getPlate());
        }
        Users owner = findOwner(request.getOwnerId());

        Vehicle vehicle = new Vehicle();
        vehicle.setPlate(request.getPlate().toUpperCase().trim());
        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setColor(request.getColor());
        vehicle.setOwner(owner);

        return toResponse(vehicleRepository.save(vehicle));
    }

    /**
     * Lista todos los vehículos registrados.
     * @return lista de VehicleResponseDTO
     */
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> list() {
        return vehicleRepository.findAll().stream().map(this::toResponse).toList();
    }

    /**
     * Obtiene un vehículo por su ID.
     * @param id ID del vehículo
     * @return VehicleResponseDTO con los datos del vehículo
     */
    @Transactional(readOnly = true)
    public VehicleResponseDTO getById(Long id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado con id: " + id));
        return toResponse(vehicle);
    }

    /**
     * Busca un vehículo por su placa.
     * @param plate placa del vehículo
     * @return VehicleResponseDTO con los datos del vehículo
     */
    @Transactional(readOnly = true)
    public VehicleResponseDTO getByPlate(String plate) {
        Vehicle vehicle = vehicleRepository.findByPlateIgnoreCase(plate)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado con placa: " + plate));
        return toResponse(vehicle);
    }

    /**
     * Criterio 4: lista todos los vehículos asociados a un cliente por su ID de usuario.
     * @param ownerId ID del usuario propietario
     * @return lista de VehicleResponseDTO del cliente
     */
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> listByOwner(Long ownerId) {
        findOwner(ownerId); // valida que el cliente exista
        return vehicleRepository.findByOwnerId(ownerId).stream().map(this::toResponse).toList();
    }

    /**
     * Criterio 4: lista todos los vehículos asociados a un cliente por su identificación (cédula).
     * @param identification cédula del cliente
     * @return lista de VehicleResponseDTO del cliente
     */
    @Transactional(readOnly = true)
    public List<VehicleResponseDTO> listByOwnerIdentification(Integer identification) {
        return vehicleRepository.findByOwnerIdentification(identification)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Criterio 5: actualiza el cliente asociado al vehículo (cambia propietario).
     * Criterio 6: refleja la asociación actualizada en la respuesta.
     * @param vehicleId  ID del vehículo
     * @param request    DTO con el ID del nuevo propietario
     * @return VehicleResponseDTO con la asociación actualizada
     */
    @Transactional
    public VehicleResponseDTO updateOwner(Long vehicleId, @Valid AssociateOwnerRequestDTO request) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new RuntimeException("Vehículo no encontrado con id: " + vehicleId));
        Users newOwner = findOwner(request.getOwnerId());
        vehicle.setOwner(newOwner);
        return toResponse(vehicleRepository.save(vehicle));
    }

    /**
     * Criterio 6: historial de órdenes de trabajo asociadas a un vehículo por placa.
     * Reutiliza WorkOrderRepository para no duplicar lógica.
     * @param plate placa del vehículo
     * @return lista de WorkOrderResponseDTO del historial del vehículo
     */
    @Transactional(readOnly = true)
    public List<com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO> getVehicleHistory(String plate) {
        return workOrderRepository.findByVehicleplateIgnoreCase(plate)
                .stream()
                .map(o -> {
                    com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO dto =
                            new com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO();
                    dto.setId(o.getId());
                    dto.setNumberorder(o.getNumberorder());
                    dto.setClientname(o.getClientname());
                    dto.setClientidentification(o.getClientidentification());
                    dto.setClientphone(o.getClientphone());
                    dto.setVehicleplate(o.getVehicleplate());
                    dto.setVehiclebrand(o.getVehiclebrand());
                    dto.setVehiclemodel(o.getVehiclemodel());
                    dto.setVehicleyear(o.getVehicleyear());
                    dto.setVehiclecolor(o.getVehiclecolor());
                    dto.setFailuresreported(o.getFailuresreported());
                    dto.setDateentry(o.getDateentry());
                    dto.setDateestimateddelivery(o.getDateestimateddelivery());
                    dto.setStateorder(o.getStateorder());
                    dto.setPriority(o.getPriority());
                    dto.setCreatedBy(o.getCreatedBy());
                    dto.setDatecreation(o.getDatecreation());
                    return dto;
                }).toList();
    }

    /**
     * Criterio 6: historial de órdenes de trabajo de un cliente por su identificación.
     * @param identification cédula del cliente
     * @return lista de WorkOrderResponseDTO del historial del cliente
     */
    @Transactional(readOnly = true)
    public List<com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO> getClientHistory(String identification) {
        return workOrderRepository.findByClientidentification(identification)
                .stream()
                .map(o -> {
                    com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO dto =
                            new com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO();
                    dto.setId(o.getId());
                    dto.setNumberorder(o.getNumberorder());
                    dto.setClientname(o.getClientname());
                    dto.setClientidentification(o.getClientidentification());
                    dto.setClientphone(o.getClientphone());
                    dto.setVehicleplate(o.getVehicleplate());
                    dto.setVehiclebrand(o.getVehiclebrand());
                    dto.setVehiclemodel(o.getVehiclemodel());
                    dto.setVehicleyear(o.getVehicleyear());
                    dto.setVehiclecolor(o.getVehiclecolor());
                    dto.setFailuresreported(o.getFailuresreported());
                    dto.setDateentry(o.getDateentry());
                    dto.setDateestimateddelivery(o.getDateestimateddelivery());
                    dto.setStateorder(o.getStateorder());
                    dto.setPriority(o.getPriority());
                    dto.setCreatedBy(o.getCreatedBy());
                    dto.setDatecreation(o.getDatecreation());
                    return dto;
                }).toList();
    }

    // ── Privados ──────────────────────────────────────────────────────────────

    private Users findOwner(Long ownerId) {
        return usersRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con id: " + ownerId));
    }

    private VehicleResponseDTO toResponse(Vehicle v) {
        VehicleResponseDTO dto = new VehicleResponseDTO();
        dto.setId(v.getId());
        dto.setPlate(v.getPlate());
        dto.setBrand(v.getBrand());
        dto.setModel(v.getModel());
        dto.setYear(v.getYear());
        dto.setColor(v.getColor());
        dto.setAssociationDate(v.getAssociationDate());
        if (v.getOwner() != null) {
            dto.setOwnerId(v.getOwner().getId());
            dto.setOwnerName(v.getOwner().getUsername());
            dto.setOwnerIdentification(v.getOwner().getIdentification());
            dto.setOwnerPhone(v.getOwner().getPhone());
            dto.setOwnerEmail(v.getOwner().getEmail());
        }
        return dto;
    }
}
