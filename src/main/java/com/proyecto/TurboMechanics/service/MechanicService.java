package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.*;
import com.proyecto.TurboMechanics.entity.Mechanic;
import com.proyecto.TurboMechanics.entity.MechanicAbsence;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.repository.MechanicAbsenceRepository;
import com.proyecto.TurboMechanics.repository.MechanicRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.enums.AbsenceType;
import com.proyecto.TurboMechanics.enums.LaborStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MechanicService {

    private final MechanicRepository           mechanicRepository;
    private final WorkOrderRepository          workOrderRepository;
    private final MechanicAbsenceRepository    absenceRepository;

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
        applyFields(mechanic, request);
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
            String position, LaborStatus laborStatus, LocalDate fromHireDate) {

        List<Mechanic> mechanics;
        boolean hasPosition = position != null && !position.isBlank();
        boolean hasStatus   = laborStatus != null;
        boolean hasDate     = fromHireDate != null;

        if (hasPosition && hasStatus && hasDate)
            mechanics = mechanicRepository.findByPositionIgnoreCaseAndLaborStatusAndHireDateGreaterThanEqual(
                    position, laborStatus, fromHireDate);
        else if (hasPosition && hasStatus)
            mechanics = mechanicRepository.findByPositionIgnoreCaseAndLaborStatus(position, laborStatus);
        else if (hasPosition)
            mechanics = mechanicRepository.findByPositionIgnoreCase(position);
        else if (hasStatus)
            mechanics = mechanicRepository.findByLaborStatus(laborStatus);
        else if (hasDate)
            mechanics = mechanicRepository.findByHireDateGreaterThanEqual(fromHireDate);
        else
            mechanics = mechanicRepository.findAll();

        // Ordenar por fecha de ingreso descendente (más reciente primero)
        mechanics.sort((a, b) -> b.getHireDate().compareTo(a.getHireDate()));
        return mechanics.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Busca un mecánico por su número de documento.
     * @param document documento del mecánico
     * @return datos del mecánico encontrado
     */
    @Transactional(readOnly = true)
    public MechanicResponseDTO getMechanicByDocument(Long document) {
        return mapToDTO(findByDocument(document));
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
        if (!mechanic.getDocument().equals(request.getDocument())
                && mechanicRepository.existsByDocument(request.getDocument())) {
            throw new RuntimeException("Ya existe un mecánico con el documento: " + request.getDocument());
        }
        applyFields(mechanic, request);
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
    public void deleteMechanic(Long document) {Mechanic mechanic = findByDocument(document);

        boolean hasActiveOrders = List.of(
                WorkOrder.StateOrder.RECIBIDO,
                WorkOrder.StateOrder.EN_DIAGNOSTICO,
                WorkOrder.StateOrder.EN_REPARACION)
                .stream()
                .flatMap(s -> workOrderRepository.findByStateorder(s).stream())
                .anyMatch(o -> mechanic.getId().equals(o.getAssignedMechanicId()));

        if (hasActiveOrders)
            throw new RuntimeException(
                    "No se puede eliminar al mecánico porque tiene órdenes de trabajo activas.");

        boolean hasPending = workOrderRepository
                .findByStateorder(WorkOrder.StateOrder.LISTO)
                .stream()
                .anyMatch(o -> mechanic.getId().equals(o.getAssignedMechanicId()));

        if (hasPending)
            throw new RuntimeException(
                    "No se puede eliminar al mecánico porque tiene historiales de trabajo pendientes por validar.");

        mechanicRepository.delete(mechanic);
    }

    /**
     * Cambia el estado laboral de un mecánico (ACTIVO, INACTIVO, SUSPENDIDO, RETIRADO).
     * Registra la fecha y el usuario que realizó el cambio (@PreUpdate en la entidad).
     *
     * @param document    documento del mecánico
     * @param newStatus   nuevo estado laboral
     * @param updatedBy   usuario que realiza el cambio
     * @return datos actualizados del mecánico
     */
    @Transactional
    public MechanicResponseDTO changeLaborStatus(
            Long document, LaborStatus newStatus, String updatedBy) {

        Mechanic mechanic = findByDocument(document);
        mechanic.setLaborStatus(newStatus);
        mechanic.setUpdatedBy(updatedBy);
        // updatedAt se asigna automáticamente en @PreUpdate
        mechanicRepository.save(mechanic);
        return mapToDTO(mechanic);
    }

    /**
     * Retorna el historial de órdenes de trabajo realizadas por un mecánico.
     * Permite filtrar por estado y por rango de fechas.
     *
     * @param document    documento del mecánico
     * @param stateorder  filtrar por estado de la orden (opcional)
     * @param from        fecha inicio del rango (opcional)
     * @param to          fecha fin del rango (opcional)
     * @return lista de órdenes del mecánico
     */
    @Transactional(readOnly = true)
    public List<WorkOrderResponseDTO> getMechanicWorkHistory(
            Long document,
            WorkOrder.StateOrder stateorder,
            LocalDateTime from,
            LocalDateTime to) {

        Mechanic mechanic = findByDocument(document);
        List<WorkOrder> orders;

        boolean hasState = stateorder != null;
        boolean hasRange = from != null && to != null;

        if (hasRange) {
            orders = workOrderRepository.findByMechanicIdAndDateRange(mechanic.getId(), from, to);
            if (hasState)
                orders = orders.stream()
                        .filter(o -> o.getStateorder() == stateorder)
                        .collect(Collectors.toList());
        } else if (hasState) {
            orders = workOrderRepository
                    .findByAssignedMechanicIdAndStateorderOrderByDatecreationDesc(
                            mechanic.getId(), stateorder);
        } else {
            orders = workOrderRepository
                    .findByAssignedMechanicIdOrderByDatecreationDesc(mechanic.getId());
        }

        return orders.stream().map(this::mapOrderToDTO).collect(Collectors.toList());
    }

    /**
     * Asigna una orden de trabajo a un mecánico validando:
     *  1. Que el mecánico esté ACTIVO.
     *  2. Que no tenga una ausencia activa ahora mismo.
     *  3. Que no haya superado su capacidad máxima de órdenes activas.
     *
     * @param orderId          id de la orden de trabajo
     * @param mechanicDocument documento del mecánico a asignar
     * @param assignedBy       usuario que realiza la asignación
     * @return datos de la orden con la asignación registrada
     */
    @Transactional
    public WorkOrderResponseDTO assignOrderToMechanic(
            Long orderId, Long mechanicDocument, String assignedBy) {

        WorkOrder order = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con id: " + orderId));

        Mechanic mechanic = findByDocument(mechanicDocument);

        if (mechanic.getLaborStatus() != LaborStatus.ACTIVO)
            throw new RuntimeException(
                    "El mecánico " + mechanic.getName() + " no está disponible (estado: "
                    + mechanic.getLaborStatus() + "). Elija otro mecánico.");

        // Validación 2: sin ausencia activa en este momento
        LocalDateTime now = LocalDateTime.now();
        boolean hasAbsence = absenceRepository.existsOverlappingAbsence(mechanic.getId(), now, now);
        if (hasAbsence)
            throw new RuntimeException(
                    "El mecánico " + mechanic.getName()
                    + " tiene una ausencia registrada para el día de hoy. Elija otro mecánico.");

        // Validación 3: capacidad de órdenes activas
        long activeCount = workOrderRepository.countActiveOrdersByMechanic(mechanic.getId());
        int maxCapacity  = mechanic.getMaxOrderCapacity() != null ? mechanic.getMaxOrderCapacity() : 3;
        if (activeCount >= maxCapacity)
            throw new RuntimeException(
                    "El mecánico " + mechanic.getName()
                    + " ya tiene " + activeCount + " órdenes activas (máximo: " + maxCapacity
                    + "). No tiene disponibilidad. Elija otro mecánico.");

        // Asignar
        order.setAssignedMechanicId(mechanic.getId());
        order.setAssignedMechanicName(mechanic.getName());
        order.setAssignedAt(now);
        order.setAssignedBy(assignedBy);

        workOrderRepository.save(order);
        return mapOrderToDTO(order);
    }

    /**
     * Reasigna una orden de trabajo a otro mecánico.
     * Aplica las mismas validaciones que la asignación inicial.
     *
     * @param orderId          id de la orden a reasignar
     * @param mechanicDocument documento del nuevo mecánico
     * @param assignedBy       usuario que realiza la reasignación
     * @return datos de la orden con la nueva asignación
     */
    @Transactional
    public WorkOrderResponseDTO reassignOrderToMechanic(
            Long orderId, Long mechanicDocument, String assignedBy) {
        // La lógica de validación es idéntica; reutilizamos el mismo método
        return assignOrderToMechanic(orderId, mechanicDocument, assignedBy);
    }

    /**
     * Registra una ausencia para un mecánico.
     *
     * @param document     documento del mecánico
     * @param request      datos de la ausencia
     * @param registeredBy usuario que registra la ausencia
     * @return datos de la ausencia registrada
     */
    @Transactional
    public MechanicAbsenceResponseDTO registerAbsence(
            Long document, MechanicAbsenceRequestDTO request, String registeredBy) {

        Mechanic mechanic = findByDocument(document);

        if (request.getEndDate().isBefore(request.getStartDate()))
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio.");

        MechanicAbsence absence = new MechanicAbsence();
        absence.setMechanic(mechanic);
        absence.setStartDate(request.getStartDate());
        absence.setEndDate(request.getEndDate());
        absence.setReason(request.getReason());
        absence.setAbsenceType(request.getAbsenceType());
        absence.setRegisteredBy(registeredBy);

        absenceRepository.save(absence);
        return mapAbsenceToDTO(absence);
    }

    /**
     * Retorna el historial de ausencias de un mecánico.
     * Permite filtrar por tipo de ausencia o por rango de fechas.
     *
     * @param document     documento del mecánico
     * @param absenceType  filtrar por tipo (opcional)
     * @param from         fecha inicio del rango (opcional)
     * @param to           fecha fin del rango (opcional)
     * @return lista de ausencias
     */
    @Transactional(readOnly = true)
    public List<MechanicAbsenceResponseDTO> getAbsenceHistory(
            Long document,
            AbsenceType absenceType,
            LocalDateTime from,
            LocalDateTime to) {

        Mechanic mechanic = findByDocument(document);
        List<MechanicAbsence> absences;

        boolean hasType  = absenceType != null;
        boolean hasRange = from != null && to != null;

        if (hasRange) {
            absences = absenceRepository.findByMechanicIdAndDateRange(mechanic.getId(), from, to);
            if (hasType)
                absences = absences.stream()
                        .filter(a -> a.getAbsenceType() == absenceType)
                        .collect(Collectors.toList());
        } else if (hasType) {
            absences = absenceRepository.findByMechanicIdAndAbsenceTypeOrderByStartDateDesc(
                    mechanic.getId(), absenceType);
        } else {
            absences = absenceRepository.findByMechanicIdOrderByStartDateDesc(mechanic.getId());
        }

        return absences.stream().map(this::mapAbsenceToDTO).collect(Collectors.toList());
    }

    private Mechanic findByDocument(Long document) {
        return mechanicRepository.findByDocument(document)
                .orElseThrow(() -> new RuntimeException(
                        "Mecánico no encontrado con el documento: " + document));
    }

    private void applyFields(Mechanic mechanic, MechanicRequestDTO request) {
        mechanic.setName(request.getName());
        mechanic.setDocument(request.getDocument());
        mechanic.setPosition(request.getPosition());
        mechanic.setHireDate(request.getHireDate());
        mechanic.setPhone(request.getPhone());
        mechanic.setEmail(request.getEmail());
        mechanic.setSalary(request.getSalary());
        if (request.getLaborStatus() != null)
            mechanic.setLaborStatus(request.getLaborStatus());
    }

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

    private WorkOrderResponseDTO mapOrderToDTO(WorkOrder order) {
        WorkOrderResponseDTO dto = new WorkOrderResponseDTO();
        dto.setId(order.getId());
        dto.setNumberorder(order.getNumberorder());
        dto.setClientname(order.getClientname());
        dto.setClientidentification(order.getClientidentification());
        dto.setClientphone(order.getClientphone());
        dto.setVehicleplate(order.getVehicleplate());
        dto.setVehiclebrand(order.getVehiclebrand());
        dto.setVehiclemodel(order.getVehiclemodel());
        dto.setVehicleyear(order.getVehicleyear());
        dto.setVehiclecolor(order.getVehiclecolor());
        dto.setFailuresreported(order.getFailuresreported());
        dto.setDateentry(order.getDateentry());
        dto.setDateestimateddelivery(order.getDateestimateddelivery());
        dto.setLevelfuel(order.getLevelfuel());
        dto.setStatescratches(order.getStatescratches());
        dto.setStatedents(order.getStatedents());
        dto.setAccessoriesobservations(order.getAccessoriesobservations());
        dto.setStateorder(order.getStateorder());
        dto.setPriority(order.getPriority());
        dto.setCreatedBy(order.getCreatedBy());
        dto.setDatecreation(order.getDatecreation());
        dto.setCancellationreason(order.getCancellationreason());
        dto.setCancellationdate(order.getCancellationdate());
        return dto;
    }

    private MechanicAbsenceResponseDTO mapAbsenceToDTO(MechanicAbsence absence) {
        MechanicAbsenceResponseDTO dto = new MechanicAbsenceResponseDTO();
        dto.setId(absence.getId());
        dto.setMechanicId(absence.getMechanic().getId());
        dto.setMechanicName(absence.getMechanic().getName());
        dto.setMechanicDocument(absence.getMechanic().getDocument());
        dto.setStartDate(absence.getStartDate());
        dto.setEndDate(absence.getEndDate());
        dto.setReason(absence.getReason());
        dto.setAbsenceType(absence.getAbsenceType());
        dto.setRegisteredBy(absence.getRegisteredBy());
        dto.setRegisteredAt(absence.getRegisteredAt());
        return dto;
    }
}