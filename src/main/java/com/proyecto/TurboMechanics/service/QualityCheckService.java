package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.*;
import com.proyecto.TurboMechanics.entity.*;
import com.proyecto.TurboMechanics.enums.QualityCheckStatus;
import com.proyecto.TurboMechanics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QualityCheckService {

    private final QualityCheckRepository  qualityCheckRepository;
    private final WorkOrderRepository     workOrderRepository;
    private final DiagnosisRepository     diagnosisRepository;

    /**
     * Inicia un control de calidad para una orden de trabajo, creando los ítems de revisión basados en el diagnóstico más reciente.
      * Si ya existe un control para la orden, lo retorna sin crear uno nuevo.
     * @param workOrderId id de la orden de trabajo
     * @param createdBy usuario que inicia el control
     * @return datos del control de calidad creado o existente para la orden de trabajo
     */
    @Transactional
    public QualityCheckResponseDTO startQualityCheck(Long workOrderId, String createdBy) {

        // Si ya existe un control para esta orden, retornarlo
        if (qualityCheckRepository.existsByWorkOrderId(workOrderId))
            return mapToDTO(qualityCheckRepository.findByWorkOrderId(workOrderId).get());

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new RuntimeException(
                        "Orden de trabajo no encontrada con id: " + workOrderId));

        QualityCheck check = new QualityCheck();
        check.setWorkOrder(workOrder);
        check.setCreatedBy(createdBy);
        check.setStatus(QualityCheckStatus.EN_PROCESO);

        // Obtener servicios del diagnóstico más reciente de la orden
        List<Diagnosis> diagnoses = diagnosisRepository.findByWorkOrderIdOrderByRegistrationdateDesc(workOrderId);
        if (!diagnoses.isEmpty()) {
            Diagnosis latest = diagnoses.get(0);
            // Crear un ítem por cada servicio mencionado en las fallas detectadas
            // En producción esto se puede reemplazar por una relación directa a ServiceEntity
            if (latest.getDetectedfailures() != null) {
                QualityCheckItem item = new QualityCheckItem();
                item.setQualityCheck(check);
                item.setServiceName("Revisión de fallas detectadas: " + latest.getDetectedfailures());
                check.getItems().add(item);
            }
        }

        // Ítem genérico de revisión final siempre presente
        QualityCheckItem generalItem = new QualityCheckItem();
        generalItem.setQualityCheck(check);
        generalItem.setServiceName("Revisión general del vehículo antes de entrega");
        check.getItems().add(generalItem);

        qualityCheckRepository.save(check);
        log.info("Control de calidad iniciado para orden {} por {}", workOrderId, createdBy);
        return mapToDTO(check);
    }

    /**
     * Actualiza los ítems de un control de calidad en proceso, permitiendo marcar cada ítem como 
     * verificado o no, y agregar observaciones.
     * @param checkId id del control de calidad a actualizar
     * @param request datos con los ítems a actualizar y observaciones generales
     * @param updatedBy usuario que realiza la actualización
     * @return datos del control de calidad actualizado
     */
    @Transactional
    public QualityCheckResponseDTO updateItems(
            Long checkId, QualityCheckRequestDTO request, String updatedBy) {

        QualityCheck check = findById(checkId);

        if (check.getStatus() == QualityCheckStatus.APROBADO
                || check.getStatus() == QualityCheckStatus.RECHAZADO)
            throw new RuntimeException(
                    "No se puede editar un control de calidad en estado: " + check.getStatus());

        // Actualizar observaciones generales si vienen en el request
        if (request.getObservations() != null)
            check.setObservations(request.getObservations());

        // Actualizar ítems
        if (request.getItems() != null) {
            request.getItems().forEach(itemReq -> {
                check.getItems().stream()
                        .filter(i -> i.getId() != null && i.getId().equals(itemReq.getItemId()))
                        .findFirst()
                        .ifPresent(item -> {
                            if (itemReq.getVerified() != null) {
                                item.setVerified(itemReq.getVerified());
                                if (Boolean.TRUE.equals(itemReq.getVerified())) {
                                    item.setVerifiedBy(updatedBy);
                                    item.setVerifiedAt(LocalDateTime.now());
                                } else {
                                    item.setVerifiedBy(null);
                                    item.setVerifiedAt(null);
                                }
                            }
                            if (itemReq.getObservation() != null)
                                item.setObservation(itemReq.getObservation());
                        });
            });
        }

        // Recalcular estado: si todos los ítems están verificados → COMPLETADO
        boolean allVerified = !check.getItems().isEmpty()
                && check.getItems().stream().allMatch(i -> Boolean.TRUE.equals(i.getVerified()));
        if (allVerified) check.setStatus(QualityCheckStatus.COMPLETADO);

        check.setUpdatedBy(updatedBy);
        qualityCheckRepository.save(check);
        return mapToDTO(check);
    }

    /**
     * Aprueba el control de calidad, indicando que el vehículo cumple con los estándares para ser entregado.
      * Solo se puede aprobar si todos los ítems están verificados. Cambia el estado a APROBADO y registra auditoría.
      * @param checkId id del control de calidad
      * @param approvedBy usuario que aprueba el control
      * @return datos del control de calidad aprobado
     */
    @Transactional
    public QualityCheckResponseDTO approveQualityCheck(Long checkId, String approvedBy) {

        QualityCheck check = findById(checkId);

        if (check.getStatus() == QualityCheckStatus.APROBADO)
            throw new RuntimeException("El control de calidad ya fue aprobado.");

        if (check.getStatus() != QualityCheckStatus.COMPLETADO) {
            long pending = check.getItems().stream()
                    .filter(i -> !Boolean.TRUE.equals(i.getVerified())).count();
            throw new RuntimeException(
                    "No se puede aprobar el control: hay " + pending
                    + " servicio(s) sin verificar.");
        }

        check.setStatus(QualityCheckStatus.APROBADO);
        check.setApprovedBy(approvedBy);
        check.setApprovedAt(LocalDateTime.now());
        check.setUpdatedBy(approvedBy);
        qualityCheckRepository.save(check);

        log.info("Control de calidad {} aprobado por {}", checkId, approvedBy);
        return mapToDTO(check);
    }

    /**
     * Rechaza el control de calidad, indicando que el vehículo no cumple con los estándares para ser entregado.
     * @param checkId id del control de calidad
     * @param observations observaciones que expliquen los motivos del rechazo
     * @param rejectedBy usuario que rechaza el control
     * @return datos del control rechazado
     */
    @Transactional
    public QualityCheckResponseDTO rejectQualityCheck(
            Long checkId, String observations, String rejectedBy) {

        QualityCheck check = findById(checkId);

        if (check.getStatus() == QualityCheckStatus.APROBADO)
            throw new RuntimeException("No se puede rechazar un control ya aprobado.");

        check.setStatus(QualityCheckStatus.RECHAZADO);
        check.setObservations(observations);
        check.setApprovedBy(rejectedBy);
        check.setApprovedAt(LocalDateTime.now());
        check.setUpdatedBy(rejectedBy);
        qualityCheckRepository.save(check);

        log.info("Control de calidad {} rechazado por {}", checkId, rejectedBy);
        return mapToDTO(check);
    }

    /**
     * Obtiene el control de calidad asociado a una orden de trabajo, incluyendo sus ítems y estado actual.
     * @param workOrderId id de la orden de trabajo
     * @return datos del control de calidad (incluyendo ítems, estado, observaciones y auditoría) o 
     * error si no existe control para la orden
     */
    @Transactional(readOnly = true)
    public QualityCheckResponseDTO getByWorkOrder(Long workOrderId) {
        QualityCheck check = qualityCheckRepository.findByWorkOrderId(workOrderId)
                .orElseThrow(() -> new RuntimeException(
                        "No existe control de calidad para la orden: " + workOrderId));
        return mapToDTO(check);
    }

    /**
     * Verifica si el control de calidad asociado a una orden de trabajo está aprobado, lo que indica 
     * que el vehículo puede ser entregado al cliente.
     * @param workOrderId id de la orden de trabajo
     * @return true si el control de calidad está aprobado, false si no existe control o no está aprobado
     */
    @Transactional(readOnly = true)
    public boolean isApproved(Long workOrderId) {
        return qualityCheckRepository.findByWorkOrderId(workOrderId)
                .map(c -> c.getStatus() == QualityCheckStatus.APROBADO)
                .orElse(false);
    }

    private QualityCheck findById(Long id) {
        return qualityCheckRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Control de calidad no encontrado con id: " + id));
    }

    private QualityCheckResponseDTO mapToDTO(QualityCheck c) {
        QualityCheckResponseDTO dto = new QualityCheckResponseDTO();
        dto.setId(c.getId());
        dto.setWorkOrderId(c.getWorkOrder().getId());
        dto.setWorkOrderNumber(c.getWorkOrder().getNumberorder());
        dto.setVehiclePlate(c.getWorkOrder().getVehicleplate());
        dto.setStatus(c.getStatus());
        dto.setObservations(c.getObservations());
        dto.setCreatedBy(c.getCreatedBy());
        dto.setCreatedAt(c.getCreatedAt());
        dto.setApprovedBy(c.getApprovedBy());
        dto.setApprovedAt(c.getApprovedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setUpdatedBy(c.getUpdatedBy());

        List<QualityCheckItemResponseDTO> itemDtos = c.getItems().stream()
                .map(this::mapItemToDTO).collect(Collectors.toList());
        dto.setItems(itemDtos);
        dto.setTotalItems(itemDtos.size());
        dto.setVerifiedItems((int) itemDtos.stream()
                .filter(i -> Boolean.TRUE.equals(i.getVerified())).count());
        return dto;
    }

    private QualityCheckItemResponseDTO mapItemToDTO(QualityCheckItem i) {
        QualityCheckItemResponseDTO dto = new QualityCheckItemResponseDTO();
        dto.setId(i.getId());
        dto.setServiceName(i.getServiceName());
        dto.setServiceId(i.getServiceId());
        dto.setVerified(i.getVerified());
        dto.setObservation(i.getObservation());
        dto.setVerifiedBy(i.getVerifiedBy());
        dto.setVerifiedAt(i.getVerifiedAt());
        return dto;
    }
}