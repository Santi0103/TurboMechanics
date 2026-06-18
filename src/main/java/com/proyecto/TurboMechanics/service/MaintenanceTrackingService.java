package com.proyecto.TurboMechanics.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.MaintenanceStatusResponseDTO;
import com.proyecto.TurboMechanics.dto.NotificationConsentRequestDTO;
import com.proyecto.TurboMechanics.dto.RegisterProgressRequestDTO;
import com.proyecto.TurboMechanics.dto.ReportIssueRequestDTO;
import com.proyecto.TurboMechanics.dto.UpdateMaintenanceTimeRequestDTO;
import com.proyecto.TurboMechanics.entity.Issue;
import com.proyecto.TurboMechanics.entity.MaintenanceProgress;
import com.proyecto.TurboMechanics.entity.NotificationConsent;
import com.proyecto.TurboMechanics.entity.NotificationLog;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.enums.NotificationChannel;
import com.proyecto.TurboMechanics.enums.StatusIssue;
import com.proyecto.TurboMechanics.repository.IssueRepository;
import com.proyecto.TurboMechanics.repository.MaintenanceProgressRepository;
import com.proyecto.TurboMechanics.repository.NotificationConsentRepository;
import com.proyecto.TurboMechanics.repository.NotificationLogRepository;
import com.proyecto.TurboMechanics.repository.UsersRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceTrackingService {

    private final WorkOrderRepository           workOrderRepository;
    private final UsersRepository               usersRepository;
    private final IssueRepository               issueRepository;
    private final MaintenanceProgressRepository progressRepository;
    private final NotificationConsentRepository consentRepository;
    private final NotificationLogRepository     notificationLogRepository;
    private final NotificationService           notificationService;

    private static final List<WorkOrder.StateOrder> INACTIVE_STATES = List.of(
            WorkOrder.StateOrder.CANCELADO,
            WorkOrder.StateOrder.ENTREGADO);

    /**
     * Estado de mantenimiento por placa
     * @param plate placa del vehiculo
     * @return retorna el estado de mantenimiento
     */
    public MaintenanceStatusResponseDTO getStatusByPlate(String plate) {
        WorkOrder workOrder = workOrderRepository
                .findFirstByVehicleplateIgnoreCaseAndStateorderNotInOrderByDateentryDesc(
                        plate, INACTIVE_STATES)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró mantenimiento activo para la placa: " + plate));
        return buildStatusResponse(workOrder);
    }

    /**
     * Estado por id de la orden de trabajo
     * @param workOrderId id de la orden de trabajo
     * @return retorna el estado de la orden de trabajo
     */
    public MaintenanceStatusResponseDTO getStatusByWorkOrderId(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden de trabajo no encontrada: " + workOrderId));
        return buildStatusResponse(workOrder);
    }

    /**
     * Historial de todas las ordenes de un vehiculo por placa
     * @param plate placa del vehiculo
     * @return retorna todas las ordenes incluyendo entregadas y canceladas
     */
    public List<MaintenanceStatusResponseDTO> getHistoryByPlate(String plate) {
        return workOrderRepository.findByVehicleplateIgnoreCase(plate)
                .stream()
                .map(this::buildStatusResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualizar fecha de entrega estimada
     * @param request dto para la fecha estimada de entrega
     * @return retorna la fecha estimada actualizada
     */
    @Transactional
    public WorkOrder updateEstimatedDelivery(UpdateMaintenanceTimeRequestDTO request) {
        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden de trabajo no encontrada: " + request.getWorkOrderId()));

        workOrder.setDateestimateddelivery(request.getEstimatedDelivery());
        WorkOrder saved = workOrderRepository.save(workOrder);

        sendNotificationToClient(
                workOrder,
                "Actualización de tiempo estimado – Orden " + workOrder.getNumberorder(),
                "El tiempo estimado de entrega de su vehículo "
                        + workOrder.getVehicleplate() + " ha sido actualizado a: "
                        + request.getEstimatedDelivery());

        log.info("Tiempo estimado actualizado para orden {}: {}",
                workOrder.getNumberorder(), request.getEstimatedDelivery());
        return saved;
    }

    /**
     * Notificar al cliente sobre el cambio de estado
     * @param workOrderId id de la orden de trabajo
     */
    public void notifyStateChange(Long workOrderId) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden de trabajo no encontrada: " + workOrderId));

        String mensaje = String.format(
                "Actualización de su vehículo %s %s (placa: %s):%n" +
                        "Estado actual: %s%n" +
                        "Fecha estimada de entrega: %s",
                workOrder.getVehiclebrand(),
                workOrder.getVehiclemodel(),
                workOrder.getVehicleplate(),
                translateState(workOrder.getStateorder()),
                workOrder.getDateestimateddelivery() != null
                        ? workOrder.getDateestimateddelivery().toString()
                        : "No disponible");

        sendNotificationToClient(
                workOrder,
                "Estado de su mantenimiento – " + workOrder.getNumberorder(),
                mensaje);
    }

    /**
     * Registrar inconveniente y notificar al cliente
     * @param request dto para el reporte del asunto
     * @return retorna el registro y notifica al cliente
     */
    @Transactional
    public Issue reportIssue(ReportIssueRequestDTO request) {
        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden de trabajo no encontrada: " + request.getWorkOrderId()));

        Issue issue = Issue.builder()
                .workOrder(workOrder)
                .description(request.getDescription())
                .status(StatusIssue.Open)
                .reportedBy(request.getReportedBy())
                .reportedAt(LocalDateTime.now())
                .build();

        Issue saved = issueRepository.save(issue);

        sendNotificationToClient(
                workOrder,
                "Inconveniente en su mantenimiento – " + workOrder.getNumberorder(),
                "Se ha registrado un inconveniente en el mantenimiento de su vehículo "
                        + workOrder.getVehicleplate() + ":\n" + request.getDescription());

        log.info("Inconveniente registrado id={} en orden {}",
                saved.getId(), workOrder.getNumberorder());
        return saved;
    }

    /**
     * Cerrar inconveniente
     * @param issueId id del asunto
     * @return cierra el inconveniente
     */
    @Transactional
    public Issue closeIssue(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Inconveniente no encontrado: " + issueId));

        if (issue.getStatus() == StatusIssue.Closed) {
            throw new IllegalStateException("El inconveniente ya está cerrado.");
        }

        issue.setStatus(StatusIssue.Closed);
        issue.setClosedAt(LocalDateTime.now());
        return issueRepository.save(issue);
    }

    /**
     * Consultar todos los inconvenientes de una orden
     * @param workOrderId id de la orden de trabajo
     * @return retorna todos los inconvenientes de una orden de trabajo
     */
    public List<Issue> getIssuesByWorkOrder(Long workOrderId) {
        return issueRepository.findByWorkOrderId(workOrderId);
    }

    /**
     * Registrar avance y notificar al cliente
     * @param request dto para registrar el progreso
     * @return retorna el registro y la notificacion al cliente
     */
    @Transactional
    public MaintenanceProgress registerProgress(RegisterProgressRequestDTO request) {
        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden de trabajo no encontrada: " + request.getWorkOrderId()));

        MaintenanceProgress progress = MaintenanceProgress.builder()
                .workOrder(workOrder)
                .description(request.getDescription())
                .registeredBy(request.getRegisteredBy())
                .registeredAt(LocalDateTime.now())
                .build();

        MaintenanceProgress saved = progressRepository.save(progress);

        sendNotificationToClient(
                workOrder,
                "Avance en su mantenimiento – " + workOrder.getNumberorder(),
                "Nuevo avance registrado para su vehículo "
                        + workOrder.getVehicleplate() + ":\n" + request.getDescription());

        log.info("Avance registrado id={} en orden {}",
                saved.getId(), workOrder.getNumberorder());
        return saved;
    }

    /**
     * Consultar avances de una orden
     * @param workOrderId id de la orden de trabajo
     * @return retorna los avances de una orden
     */
    public List<MaintenanceProgress> getProgressByWorkOrder(Long workOrderId) {
        return progressRepository.findByWorkOrderIdOrderByRegisteredAtAsc(workOrderId);
    }

    /**
     * Consultar historial de notificaciones de una orden
     * @param workOrderId id de la orden de trabajo
     * @return retorna el historial de notificaciones enviadas
     */
    public List<NotificationLog> getNotificationsByWorkOrder(Long workOrderId) {
        return notificationLogRepository.findByWorkOrderIdOrderByFechaEnvioDesc(workOrderId);
    }

    /**
     * Registro o actualizar el consentimiento
     * @param request dto para el consentimiento de la notificacion
     * @return retorna el consentimiento guardado o actualizado
     */
    @Transactional
    public NotificationConsent saveConsent(NotificationConsentRequestDTO request) {

        if (Boolean.TRUE.equals(request.getAuthorized()) && request.getChannel() == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un canal de notificación (EMAIL, WHATSAPP o BOTH).");
        }

        Users users = usersRepository.findByIdentification(request.getIdentification())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente no encontrado con cédula: " + request.getIdentification()));

        NotificationConsent consent = consentRepository
                .findByUsersIdentification(request.getIdentification())
                .orElse(NotificationConsent.builder().users(users).build());

        consent.setAuthorized(request.getAuthorized());
        consent.setChannel(Boolean.TRUE.equals(request.getAuthorized())
                ? request.getChannel()
                : null);

        NotificationConsent saved = consentRepository.save(consent);
        log.info("Consentimiento guardado para cliente {}: autorizado={} canal={}",
                request.getIdentification(), request.getAuthorized(), request.getChannel());
        return saved;
    }

    /**
     * Consultar el consentimiento de un cliente
     * @param identification identificacion del cliente
     * @return retorna el consentimiento del cliente
     */
    public NotificationConsent getConsent(Integer identification) {
        return consentRepository.findByUsersIdentification(identification)
                .orElseThrow(() -> new EntityNotFoundException(
                        "No se encontró consentimiento para la cédula: " + identification));
    }

    /**
     * Enviar notificacion al cliente y persistir en historial
     * @param workOrder orden de trabajo del cliente
     * @param subject asunto de la notificacion
     * @param body cuerpo de la notificacion
     */
    private void sendNotificationToClient(WorkOrder workOrder, String subject, String body) {
        try {
            Integer identification = Integer.parseInt(workOrder.getClientidentification());

            NotificationConsent consent = consentRepository
                    .findByUsersIdentification(identification).orElse(null);

            if (consent == null || !consent.isAuthorized()) {
                log.info("Cliente {} no autorizó notificaciones. No se envía.", identification);
                return;
            }

            Users users = usersRepository.findByIdentification(identification).orElse(null);
            if (users == null) return;

            NotificationChannel channel = consent.getChannel();
            String canalUsado = channel != null ? channel.name() : "DESCONOCIDO";

            if (channel == NotificationChannel.Email || channel == NotificationChannel.Both) {
                notificationService.SentEmailText(users.getEmail(), subject, body);
            }
            if (channel == NotificationChannel.Whatsapp || channel == NotificationChannel.Both) {
                notificationService.SendWhatsappText(users.getPhone(), body);
            }

            NotificationLog logEntry = NotificationLog.builder()
                    .workOrder(workOrder)
                    .canal(canalUsado)
                    .asunto(subject)
                    .cuerpo(body)
                    .fechaEnvio(LocalDateTime.now())
                    .build();
            notificationLogRepository.save(logEntry);

            log.info("Notificación enviada y registrada para orden {}. Canal: {}",
                    workOrder.getNumberorder(), canalUsado);

        } catch (NumberFormatException e) {
            log.warn("No se pudo parsear la identificación del cliente: {}",
                    workOrder.getClientidentification());
        } catch (Exception e) {
            log.error("Error enviando notificación para orden {}: {}",
                    workOrder.getNumberorder(), e.getMessage());
        }
    }

    /**
     * Arma el dto de respuesta desde el workOrder
     * @param wo parametro de la entity
     * @return retorna el dto de respuesta
     */
    private MaintenanceStatusResponseDTO buildStatusResponse(WorkOrder wo) {
        return new MaintenanceStatusResponseDTO(
                wo.getId(),
                wo.getNumberorder(),
                wo.getStateorder(),
                wo.getAssignedMechanicName() != null
                        ? wo.getAssignedMechanicName()
                        : "Aún no se ha asignado un mecánico",
                wo.getFailuresreported(),
                wo.getDateentry(),
                wo.getDateestimateddelivery(),
                wo.getVehiclebrand(),
                wo.getVehiclemodel(),
                wo.getVehicleplate());
    }

    /**
     * Traduce el StateOrder para los mensajes de notificacion
     * @param state parametro para el enum stateorder
     * @return retorna los mensajes de notificacion traducidos
     */
    private String translateState(WorkOrder.StateOrder state) {
        return switch (state) {
            case RECIBIDO       -> "Recibido";
            case EN_DIAGNOSTICO -> "En diagnóstico";
            case EN_REPARACION  -> "En reparación";
            case LISTO          -> "Listo para entrega";
            case ENTREGADO      -> "Entregado";
            case CANCELADO      -> "Cancelado";
        };
    }
}