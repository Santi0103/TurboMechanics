package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.DiagnosisRequestDTO;
import com.proyecto.TurboMechanics.dto.DiagnosisResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO;
import com.proyecto.TurboMechanics.entity.Diagnosis;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.repository.DiagnosisRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderService    workOrderService;

    /**
     * Registra un nuevo diagnóstico técnico asociado a una orden de trabajo.
     * Valida que la orden exista y que los campos obligatorios estén completos.
     * Actualiza el estado de la orden a EN_DIAGNOSTICO.
     *
     * @param request DTO con los datos del diagnóstico, validado por Spring
     * @return DiagnosisResponseDTO con el diagnóstico guardado
     */
    @Transactional
    public DiagnosisResponseDTO create(@Valid DiagnosisRequestDTO request) {
        WorkOrder order = findOrder(request.getWorkOrderId());

        if (order.getStateorder() == WorkOrder.StateOrder.CANCELADO) {
            throw new IllegalStateException(
                    "No se puede registrar un diagnóstico para una orden cancelada.");
        }
        if (order.getStateorder() == WorkOrder.StateOrder.ENTREGADO) {
            throw new IllegalStateException(
                    "No se puede registrar un diagnóstico para una orden ya entregada.");
        }

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setWorkOrder(order);
        diagnosis.setDetectedfailures(request.getDetectedfailures());
        diagnosis.setMechanicobservations(request.getMechanicobservations());
        diagnosis.setUrgencylevel(request.getUrgencylevel());
        diagnosis.setRegisteredby(request.getRegisteredby());

        // Avanzar estado a EN_DIAGNOSTICO si aún estaba en RECIBIDO
        if (order.getStateorder() == WorkOrder.StateOrder.RECIBIDO) {
            order.setStateorder(WorkOrder.StateOrder.EN_DIAGNOSTICO);
            workOrderRepository.save(order);
        }

        Diagnosis saved = diagnosisRepository.save(diagnosis);
        return toResponse(saved);
    }

    /**
     * Actualiza un diagnóstico existente. Solo se pueden actualizar los campos de fallas, observaciones, urgencia y la orden asociada (si se requiere cambiar de orden).
     * No se puede actualizar un diagnóstico que ya fue utilizado para generar una orden de trabajo.
     * @param id      ID del diagnóstico a actualizar
     * @param request DTO con los nuevos datos
     * @return DiagnosisResponseDTO actualizado
     */
    @Transactional
    public DiagnosisResponseDTO update(Long id, @Valid DiagnosisRequestDTO request) {
        Diagnosis diagnosis = findDiagnosis(id);

        if (diagnosis.isOrdergenerated()) {
            throw new IllegalStateException(
                    "No se puede editar el diagnóstico: ya fue utilizado para generar una orden de trabajo.");
        }

        // Verificar que la orden sigue siendo la misma o actualizar si se pide cambiar
        if (!diagnosis.getWorkOrder().getId().equals(request.getWorkOrderId())) {
            WorkOrder newOrder = findOrder(request.getWorkOrderId());
            diagnosis.setWorkOrder(newOrder);
        }

        diagnosis.setDetectedfailures(request.getDetectedfailures());
        diagnosis.setMechanicobservations(request.getMechanicobservations());
        diagnosis.setUrgencylevel(request.getUrgencylevel());
        if (request.getRegisteredby() != null) {
            diagnosis.setRegisteredby(request.getRegisteredby());
        }

        Diagnosis saved = diagnosisRepository.save(diagnosis);
        return toResponse(saved);
    }

    /**
     * Lista todos los diagnósticos registrados para una orden de trabajo.
     * @param workOrderId ID de la orden de trabajo
     * @return Lista de DiagnosisResponseDTO ordenada por fecha de registro descendente
     */
    @Transactional(readOnly = true)
    public List<DiagnosisResponseDTO> listByWorkOrder(Long workOrderId) {
        return diagnosisRepository
                .findByWorkOrderIdOrderByRegistrationdateDesc(workOrderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtiene un diagnóstico por su ID.
     * @param id ID del diagnóstico
     * @return DiagnosisResponseDTO con los datos del diagnóstico
     */
    @Transactional(readOnly = true)
    public DiagnosisResponseDTO getById(Long id) {
        return toResponse(findDiagnosis(id));
    }

    /**
     * Genera una nueva orden de trabajo a partir del diagnóstico registrado.
     * Marca el diagnóstico como utilizado para evitar duplicados.
     * La nueva orden hereda los datos del cliente y vehículo, y usa las fallas detectadas
     * como fallas reportadas.
     *
     * @param diagnosisId ID del diagnóstico
     * @param createdBy   Usuario que genera la orden
     * @return WorkOrderResponseDTO con la nueva orden creada
     */
    @Transactional
    public WorkOrderResponseDTO generateWorkOrderFromDiagnosis(Long diagnosisId, String createdBy) {
        Diagnosis diagnosis = findDiagnosis(diagnosisId);

        if (diagnosis.isOrdergenerated()) {
            throw new IllegalStateException(
                    "Ya se generó una orden de trabajo desde este diagnóstico.");
        }

        WorkOrder source = diagnosis.getWorkOrder();

        // Construir el RequestDTO de la nueva orden a partir del diagnóstico
        com.proyecto.TurboMechanics.dto.WorkOrderRequestDTO req =
                new com.proyecto.TurboMechanics.dto.WorkOrderRequestDTO();
        req.setClientname(source.getClientname());
        req.setClientidentification(source.getClientidentification());
        req.setClientphone(source.getClientphone());
        req.setVehicleplate(source.getVehicleplate());
        req.setVehiclebrand(source.getVehiclebrand());
        req.setVehiclemodel(source.getVehiclemodel());
        req.setVehicleyear(source.getVehicleyear());
        req.setVehiclecolor(source.getVehiclecolor());
        // Las fallas reportadas en la nueva orden son las detectadas en el diagnóstico
        req.setFailuresreported(diagnosis.getDetectedfailures());
        req.setLevelfuel(source.getLevelfuel());
        req.setStatescratches(source.getStatescratches());
        req.setStatedents(source.getStatedents());
        req.setAccessoriesobservations(
                "Diagnóstico: " + diagnosis.getMechanicobservations() + "\n" +
                (source.getAccessoriesobservations() != null ? source.getAccessoriesobservations() : ""));
        // Mapear urgencia → prioridad
        req.setPriority(mapUrgencyToPriority(diagnosis.getUrgencylevel()));
        req.setCreatedBy(createdBy != null ? createdBy : diagnosis.getRegisteredby());

        WorkOrderResponseDTO newOrder = workOrderService.create(req);

        // Marcar diagnóstico como utilizado
        diagnosis.setOrdergenerated(true);
        diagnosisRepository.save(diagnosis);

        return newOrder;
    }

    private WorkOrder findOrder(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de trabajo no encontrada con id: " + id));
    }

    private Diagnosis findDiagnosis(Long id) {
        return diagnosisRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnóstico no encontrado con id: " + id));
    }

    private WorkOrder.Priority mapUrgencyToPriority(Diagnosis.UrgencyLevel level) {
        return switch (level) {
            case BAJO    -> WorkOrder.Priority.BAJA;
            case MEDIO   -> WorkOrder.Priority.NORMAL;
            case ALTO    -> WorkOrder.Priority.ALTA;
            case CRITICO -> WorkOrder.Priority.URGENTE;
        };
    }

    private DiagnosisResponseDTO toResponse(Diagnosis d) {
        DiagnosisResponseDTO dto = new DiagnosisResponseDTO();
        dto.setId(d.getId());
        dto.setWorkOrderId(d.getWorkOrder().getId());
        dto.setWorkOrderNumber(d.getWorkOrder().getNumberorder());
        dto.setVehicleplate(d.getWorkOrder().getVehicleplate());
        dto.setDetectedfailures(d.getDetectedfailures());
        dto.setMechanicobservations(d.getMechanicobservations());
        dto.setUrgencylevel(d.getUrgencylevel());
        dto.setOrdergenerated(d.isOrdergenerated());
        dto.setRegisteredby(d.getRegisteredby());
        dto.setRegistrationdate(d.getRegistrationdate());
        dto.setUpdatedate(d.getUpdatedate());
        return dto;
    }
}
