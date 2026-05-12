package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.MechanicRequestDTO;
import com.proyecto.TurboMechanics.dto.MechanicResponseDTO;
import com.proyecto.TurboMechanics.entity.Mechanic;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.repository.MechanicRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.proyecto.TurboMechanics.enums.LaborStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MechanicService {

    private final MechanicRepository mechanicRepository;
    private final WorkOrderRepository workOrderRepository;

    /**
     * Registra un nuevo mecánico en el sistema.
     * @param request datos del mecánico a registrar
     * @param createdBy usuario administrador que realiza el registro
     * @return datos del mecánico registrado
     */
    @Transactional
    public MechanicResponseDTO registerMechanic(MechanicRequestDTO request, String createdBy) {

        if (mechanicRepository.existsByDocument(request.getDocument())) {
            throw new RuntimeException(
                    "Ya existe un mecánico registrado con el documento: " + request.getDocument());
        }

        Mechanic mechanic = new Mechanic();
        mechanic.setName(request.getName());
        mechanic.setDocument(request.getDocument());
        mechanic.setPosition(request.getPosition());
        mechanic.setHireDate(request.getHireDate());
        mechanic.setPhone(request.getPhone());
        mechanic.setEmail(request.getEmail());
        mechanic.setSalary(request.getSalary());
        mechanic.setLaborStatus(
                request.getLaborStatus() != null ? request.getLaborStatus() : LaborStatus.ACTIVO);
        mechanic.setCreatedBy(createdBy);

        mechanicRepository.save(mechanic);
        return mapToDTO(mechanic);
    }

    /**
     * Retorna el listado completo de mecánicos, con filtros opcionales.
     * @param position     filtrar por cargo (puede ser null)
     * @param laborStatus  filtrar por estado laboral (puede ser null)
     * @param fromHireDate filtrar por fecha de ingreso >= fecha indicada (puede ser null)
     * @return lista de mecánicos que cumplen los criterios
     */
    @Transactional(readOnly = true)
    public List<MechanicResponseDTO> getMechanics(
            String position,
            LaborStatus laborStatus,
            LocalDate fromHireDate) {

        List<Mechanic> mechanics;

        boolean hasPosition   = position != null && !position.isBlank();
        boolean hasStatus     = laborStatus != null;
        boolean hasDate       = fromHireDate != null;

        if (hasPosition && hasStatus && hasDate) {
            mechanics = mechanicRepository
                    .findByPositionIgnoreCaseAndLaborStatusAndHireDateGreaterThanEqual(
                            position, laborStatus, fromHireDate);
        } else if (hasPosition && hasStatus) {
            mechanics = mechanicRepository
                    .findByPositionIgnoreCaseAndLaborStatus(position, laborStatus);
        } else if (hasPosition) {
            mechanics = mechanicRepository.findByPositionIgnoreCase(position);
        } else if (hasStatus) {
            mechanics = mechanicRepository.findByLaborStatus(laborStatus);
        } else if (hasDate) {
            mechanics = mechanicRepository.findByHireDateGreaterThanEqual(fromHireDate);
        } else {
            mechanics = mechanicRepository.findAll();
        }

        // Ordenar por fecha de ingreso descendente (más reciente primero)
        mechanics.sort((a, b) -> b.getHireDate().compareTo(a.getHireDate()));

        return mechanics.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Busca un mecánico por su número de documento.
     * @param document documento del mecánico
     * @return datos del mecánico encontrado
     */
    @Transactional(readOnly = true)
    public MechanicResponseDTO getMechanicByDocument(Long document) {
        Mechanic mechanic = findByDocument(document);
        return mapToDTO(mechanic);
    }

    /**
     * Actualiza los datos de un mecánico existente.
     * @param document    documento del mecánico a actualizar
     * @param request     nuevos datos
     * @param updatedBy   usuario administrador que realiza la actualización
     * @return datos actualizados del mecánico
     */
    @Transactional
    public MechanicResponseDTO updateMechanic(Long document, MechanicRequestDTO request, String updatedBy) {

        Mechanic mechanic = findByDocument(document);

        if (!mechanic.getDocument().equals(request.getDocument())) {
            if (mechanicRepository.existsByDocument(request.getDocument())) {
                throw new RuntimeException(
                        "Ya existe un mecánico con el documento: " + request.getDocument());
            }
        }

        mechanic.setName(request.getName());
        mechanic.setDocument(request.getDocument());
        mechanic.setPosition(request.getPosition());
        mechanic.setHireDate(request.getHireDate());
        mechanic.setPhone(request.getPhone());
        mechanic.setEmail(request.getEmail());
        mechanic.setSalary(request.getSalary());
        if (request.getLaborStatus() != null) {
            mechanic.setLaborStatus(request.getLaborStatus());
        }
        mechanic.setUpdatedBy(updatedBy);
        mechanicRepository.save(mechanic);
        return mapToDTO(mechanic);
    }

    /**
     * Elimina un mecánico del sistema.
     * Valida que no tenga órdenes de trabajo activas ni historiales pendientes.
     * @param document documento del mecánico a eliminar
     */
    @Transactional
    public void deleteMechanic(Long document) {
        Mechanic mechanic = findByDocument(document);

        List<WorkOrder.StateOrder> activeStates = List.of(
                WorkOrder.StateOrder.RECIBIDO,
                WorkOrder.StateOrder.EN_DIAGNOSTICO,
                WorkOrder.StateOrder.EN_REPARACION
        );

        boolean hasActiveOrders = activeStates.stream()
                .flatMap(state -> workOrderRepository.findByStateorder(state).stream())
                .anyMatch(order -> order.getCreatedBy() != null
                        && order.getCreatedBy().equals(mechanic.getName()));

        if (hasActiveOrders) {
            throw new RuntimeException(
                    "No se puede eliminar al mecánico porque tiene órdenes de trabajo activas.");
        }

        boolean hasPendingHistory = workOrderRepository
                .findByStateorder(WorkOrder.StateOrder.LISTO)
                .stream()
                .anyMatch(order -> order.getCreatedBy() != null
                        && order.getCreatedBy().equals(mechanic.getName()));

        if (hasPendingHistory) {
            throw new RuntimeException(
                    "No se puede eliminar al mecánico porque tiene historiales de trabajo pendientes por validar.");
        }

        mechanicRepository.delete(mechanic);
    }

    /**
     * Busca un mecánico por documento o lanza excepción si no existe.
     */
    private Mechanic findByDocument(Long document) {
        return mechanicRepository.findByDocument(document)
                .orElseThrow(() -> new RuntimeException(
                        "Mecánico no encontrado con el documento: " + document));
    }

    /**
     * Mapea la entidad Mechanic a MechanicResponseDTO.
     */
    private MechanicResponseDTO mapToDTO(Mechanic mechanic) {
        MechanicResponseDTO dto = new MechanicResponseDTO();
        dto.setId(mechanic.getId());
        dto.setName(mechanic.getName());
        dto.setDocument(mechanic.getDocument());
        dto.setPosition(mechanic.getPosition());
        dto.setHireDate(mechanic.getHireDate());
        dto.setPhone(mechanic.getPhone());
        dto.setEmail(mechanic.getEmail());
        dto.setSalary(mechanic.getSalary());
        dto.setLaborStatus(mechanic.getLaborStatus());
        dto.setCreatedBy(mechanic.getCreatedBy());
        dto.setCreatedAt(mechanic.getCreatedAt());
        dto.setUpdatedAt(mechanic.getUpdatedAt());
        dto.setUpdatedBy(mechanic.getUpdatedBy());
        return dto;
    }
}