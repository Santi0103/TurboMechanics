package com.proyecto.TurboMechanics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.SentEstimateRequestDTO;
import com.proyecto.TurboMechanics.entity.Estimate;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.entity.Vehicle;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.enums.StatusEstimate;
import com.proyecto.TurboMechanics.repository.EstimateRepository;
import com.proyecto.TurboMechanics.repository.UsersRepository;
import com.proyecto.TurboMechanics.repository.VehicleRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EstimateService {

    private final EstimateRepository estimateRepository;

    private final UsersRepository usersRepository;

    private final VehicleRepository vehicleRepository;

    private final WorkOrderRepository workOrderRepository;

    private final NotificationService notificationService;

    /**URL base del fornt para aprobar el presupuesto */
    @Value("${app.frontend.url:http://localhost:4200/estima-confirmation}")
    private String frontendUrl;


    /**
     * enviar presupuesto al cliente
     * @param request dto para enviar el presupuesto
     * @return retorna el envio del presupuesto
     */
    @Transactional
    public Estimate sendEstimate(SentEstimateRequestDTO request) {

        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden de trabajo no encontrada: " + request.getWorkOrderId()));

        Users users = usersRepository.findByIdentification(request.getIdentification())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente no encontrado con documento: " + request.getIdentification()));

        Vehicle vehiculo = vehicleRepository.findByPlateIgnoreCase(request.getPlate().toUpperCase())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Vehículo no encontrado con placa: " + request.getPlate()));

        Estimate estimate = Estimate.builder()
                .workOrder(workOrder)
                .users(users)
                .vehicle(vehiculo)
                .description(request.getDescription())
                .totalEstimate(request.getTotalEstimate())
                .statusEstimate(StatusEstimate.SENT)
                .dateSent(LocalDateTime.now())
                .build();

        Estimate saved = estimateRepository.save(estimate);

        String menssage = buildHtmlMessage(saved);
        String canal = request.getCanal();

        if ("EMAIL".equalsIgnoreCase(canal)) {
            notificationService.SendEmailHtml(
                    users.getEmail(),
                    "Presupuesto de servicios – Turbo Mechanics",
                    menssage);
        } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
            notificationService.SendWhatsappText(users.getPhone(), buildWhatsappMessage(saved));
        } else {
            throw new IllegalArgumentException("Canal no válido: " + canal + ". Use EMAIL o WHATSAPP");
        }

        return saved;
    }

    /**
     * El cliente aprueba o rechaza el presupuesto a traves del correo
     * @param token token del cliente
     * @param approved aprovado o rechazado del cliente
     * @return retorna la respuesta del cliente
     */
    @Transactional
    public Estimate responseByToken(String token, boolean approved) {
        Estimate estimate = estimateRepository.findByToken(token)
            .orElseThrow(() -> new EntityNotFoundException(
                "Presupuesto no encontrado o enlace inválido."));
 
        if (estimate.getStatusEstimate() != StatusEstimate.SENT) {
            throw new IllegalStateException(
                "El presupuesto ya fue respondido con estado: "
                    + estimate.getStatusEstimate());
        }
 
        estimate.setStatusEstimate(approved ? StatusEstimate.APPROVED : StatusEstimate.REJECTED);
        estimate.setDateResponse(LocalDateTime.now());
        return estimateRepository.save(estimate);
    }

    /**
     * El cliente aprueba o rechaza por id (uso del admin)
     * @param estimateId id del presupuesto
     * @param approved aprobado o rechazado del cliente
     * @return retorna una respuesta
     */
    @Transactional
    public Estimate response(Long estimateId, boolean approved) {
        Estimate estimate = estimateRepository.findById(estimateId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Presupuesto no encontrado: " + estimateId));

        if (estimate.getStatusEstimate() != StatusEstimate.SENT) {
            throw new IllegalStateException(
                    "El presupuesto ya fue respondido con estado: " + estimate.getStatusEstimate());
        }

        estimate.setStatusEstimate(approved ? StatusEstimate.APPROVED : StatusEstimate.REJECTED);
        estimate.setDateResponse(LocalDateTime.now());
        return estimateRepository.save(estimate);
    }

    /**
     * Lista por cliente
     * @param identification identificacion del cliente
     * @param plate placa del vehiculo
     * @return retorna la lista del cliente
     */
    public List<Estimate> listByClient(Integer identification, String plate) {
        if (plate != null && !plate.isBlank()) {
            return estimateRepository.findByUsersIdentificationAndVehiclePlate(
                    identification, plate.toUpperCase());
        }
        return estimateRepository.findByUsersIdentification(identification);
    }

    /**
     * correo html 
     * @param p parametro del la entidad estimate
     * @return retorna el correo
     */
     private String buildHtmlMessage(Estimate p) {
        String approveUrl = frontendUrl
            + "/estima-confirmation/"
            + p.getToken()
            + "/aprobar";

        String rejectUrl = frontendUrl
            + "/estima-confirmation/"
            + p.getToken()
            + "/rechazar";
 
        return "<!DOCTYPE html>" +
            "<html lang='es'><head><meta charset='UTF-8'>" +
            "<style>" +
            "  body { font-family: Arial, sans-serif; color: #333; margin: 0; padding: 0; }" +
            "  .container { max-width: 600px; margin: 30px auto; padding: 24px;" +
            "    border: 1px solid #e0e0e0; border-radius: 8px; }" +
            "  h2 { color: #1a1a2e; }" +
            "  .detail-box { background: #f9f9f9; border-radius: 6px;" +
            "    padding: 16px; margin: 16px 0; font-size: 14px; }" +
            "  .total { font-size: 18px; font-weight: bold; color: #1a1a2e; margin: 12px 0; }" +
            "  .btn { display: inline-block; padding: 12px 28px; border-radius: 6px;" +
            "    text-decoration: none; font-weight: bold; font-size: 15px; margin: 8px 6px 8px 0; }" +
            "  .btn-approve { background-color: #28a745; color: #fff; }" +
            "  .btn-reject  { background-color: #dc3545; color: #fff; }" +
            "  .footer { font-size: 12px; color: #888; margin-top: 24px; }" +
            "</style></head><body>" +
            "<div class='container'>" +
            "  <h2>Presupuesto de servicios – TurboMechanics</h2>" +
            "  <p>Estimado/a <strong>" + p.getUsers().getUsername() + "</strong>,</p>" +
            "  <p>Hemos preparado el presupuesto para su vehículo con placa " +
            "     <strong>" + p.getVehicle().getPlate() + "</strong>.</p>" +
            "  <div class='detail-box'>" +
            "    <strong>Detalle del servicio:</strong><br/>" +
            "    <pre style='white-space:pre-wrap;margin:8px 0'>" + p.getDescription() + "</pre>" +
            "  </div>" +
            "  <p class='total'>Total estimado: $" + p.getTotalEstimate() + "</p>" +
            "  <p>Por favor, revise el presupuesto y seleccione una opción:</p>" +
            "  <a href='" + approveUrl + "' class='btn btn-approve'>✔ Aprobar presupuesto</a>" +
            "  <a href='" + rejectUrl  + "' class='btn btn-reject' >✖ Rechazar presupuesto</a>" +
            "  <p class='footer'>" +
            "    Este enlace es de uso único. Si tiene dudas, comuníquese con nosotros.<br/>" +
            "    © TurboMechanics" +
            "  </p>" +
            "</div></body></html>";
    }

    /**
     * mensjae de whatsapp
     * @param p parametro de la entidad estimate
     * @return retorna el mensaje de whastapp
     */
    private String buildWhatsappMessage(Estimate p) {
        String url = frontendUrl + "/presupuesto/responder?token=" + p.getToken();
        return String.format(
            "Hola %s 👋%n%n" +
            "Le informamos que su presupuesto para el vehículo *%s* está listo.%n%n" +
            "💰 *Total estimado:* $%s%n%n" +
            "Para APROBAR o RECHAZAR el presupuesto, ingrese al siguiente enlace:%n%s%n%n" +
            "Gracias por confiar en *TurboMechanics* 🔧",
            p.getUsers().getUsername(),
            p.getVehicle().getPlate(),
            p.getTotalEstimate(),
            url
        );
    }
}