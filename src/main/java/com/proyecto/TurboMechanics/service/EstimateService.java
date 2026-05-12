package com.proyecto.TurboMechanics.service;

import java.time.LocalDateTime;
import java.util.List;

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

    private final EstimateRepository  estimateRepository;

    private final UsersRepository        usersRepository;

    private final VehicleRepository      vehicleRepository;

    private final WorkOrderRepository workOrderRepository;

    private final NotificationService    notificationService;
 
    /**
     * Enviar el presupuesto
     * @param request dto para enviar el presupuesto
     * @return retorna el envio del presupuesto
     */
    @Transactional
    public Estimate sendtEstimate(SentEstimateRequestDTO request) {
 
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
            .detailJson(request.getDetailJson())
            .totalEstimate(request.getTotalEstimate())
            .statusEstimate(StatusEstimate.SENT)
            .dateSent(LocalDateTime.now())
            .build();
 
        Estimate saved = estimateRepository.save(estimate);
 
        String menssage = buildMessage(saved);
        String canal   = request.getCanal();
 
        if ("EMAIL".equalsIgnoreCase(canal)) {
            notificationService.SentEmailText(
                users.getEmail(),
                "Presupuesto de servicios – Taller",
                menssage
            );
        } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
            notificationService.SentWhatsappText(users.getPhone(), menssage);
        } else {
            throw new IllegalArgumentException("Canal no válido: " + canal + ". Use EMAIL o WHATSAPP");
        }
 
        return saved;
    }
 
    /**
     * Respuesta del cliente si aprueba o no
     * @param estimateId id del presupuesto
     * @param approved apropado o no por parte del cliente
     * @return retorna una respuesta
     */
    @Transactional
    public Estimate response(Long estimateId, boolean approved) {
        Estimate estimate = estimateRepository.findById(estimateId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Presupuesto no encontrado: " + approved));
 
        if (estimate.getStatusEstimate() != StatusEstimate.SENT) {
            throw new IllegalStateException(
                "El presupuesto ya fue respondido con estado: " + estimate.getStatusEstimate());
        }
 
        estimate.setStatusEstimate(approved ? StatusEstimate.APPROVED : StatusEstimate.REJECTED);
        estimate.setDateResponse(LocalDateTime.now());
        return estimateRepository.save(estimate);
    }
 
    /**
     * filtra por identificacion del cliente o placa
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
     * contruir el mensaje
     * @param p entidad del presupuesto
     * @return retorna la creacion del mensaje
     */
    private String buildMessage(Estimate p) {
        return String.format(
            "Estimado/a %s,%n%n" +
            "Le informamos que hemos preparado el presupuesto para su vehículo con placa %s.%n%n" +
            "Detalle:%n%s%n%n" +
            "Total estimado: $%s%n%n" +
            "Para APROBAR o RECHAZAR el presupuesto, responda este mensaje o ingrese al sistema.%n%n" +
            "Gracias por confiar en nosotros.",
            p.getUsers().getUsername(),
            p.getVehicle().getPlate(),
            p.getDetailJson(),
            p.getTotalEstimate()
        );
    }
}
