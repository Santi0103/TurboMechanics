package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.WarrantyResponseDTO;
import com.proyecto.TurboMechanics.dto.WarrantyValidationResponseDTO;
import com.proyecto.TurboMechanics.entity.Warranty;
import com.proyecto.TurboMechanics.entity.WarrantyValidation;
import com.proyecto.TurboMechanics.enums.WarrantyValidationStatus;
import com.proyecto.TurboMechanics.repository.WarrantyRepository;
import com.proyecto.TurboMechanics.repository.WarrantyValidationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.proyecto.TurboMechanics.enums.WarrantyStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarrantyHistoryAndValidationService {

    private final WarrantyRepository           warrantyRepository;
    private final WarrantyValidationRepository  validationRepository;

    /**
     * Retorna el historial completo de garantías asociadas a un cliente por su identificación,
     * @param clientIdentification identificación del cliente
     * @return lista de garantías asociadas al cliente, ordenadas por fecha de creación
     */
    @Transactional(readOnly = true)
    public List<WarrantyResponseDTO> getHistoryByClient(String clientIdentification) {
        return warrantyRepository
                .findByWorkOrderClientidentificationOrderByCreatedAtDesc(clientIdentification)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retorna el historial completo de garantías asociadas a un vehículo por su placa,
     * @param vehiclePlate placa del vehículo
     * @return lista de garantías asociadas al vehículo, ordenadas por fecha de creación
     */
    @Transactional(readOnly = true)
    public List<WarrantyResponseDTO> getHistoryByVehicle(String vehiclePlate) {
        return warrantyRepository
                .findByWorkOrderVehicleplateIgnoreCaseOrderByCreatedAtDesc(vehiclePlate)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Permite buscar en el historial de garantías por texto libre, buscando coincidencias
     * @param text texto de búsqueda que puede coincidir con cliente, vehículo, servicio o repuesto
     * @return lista de garantías que coinciden con el texto de búsqueda, ordenadas por fecha de creación
     */
    @Transactional(readOnly = true)
    public List<WarrantyResponseDTO> searchHistory(String text) {
        return warrantyRepository.searchByText(text)
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Valida la vigencia de una garantía por su id, determinando si está vigente, vencida o cerrada,
     * @param warrantyId id de la garantía a validar
     * @param validatedBy usuario que realiza la validación (para auditoría)
     * @return resultado de la validación, incluyendo estado de vigencia, mensaje explicativo y motivo de rechazo si aplica
     */
    @Transactional
    public WarrantyValidationResponseDTO validateWarranty(Long warrantyId, String validatedBy) {

        Warranty warranty = warrantyRepository.findById(warrantyId)
                .orElseThrow(() -> new RuntimeException(
                        "Garantía no encontrada con id: " + warrantyId));

        LocalDate today = LocalDate.now();
        WarrantyValidationStatus result;
        boolean coverageApproved;
        String message;
        String rejectionReason = null;

        // Determinar resultado
        if (warranty.getStatus() == WarrantyStatus.CERRADA) {
            result           = WarrantyValidationStatus.CERRADA;
            coverageApproved = false;
            message          = "La garantía fue cerrada el "
                    + warranty.getClosureDate().toLocalDate()
                    + ". No es posible aprobar cobertura.";
            rejectionReason  = "Garantía cerrada: " + warranty.getClosureReason();

        } else if (today.isAfter(warranty.getEndDate())) {
            result           = WarrantyValidationStatus.VENCIDA;
            coverageApproved = false;
            message          = "La garantía venció el " + warranty.getEndDate()
                    + ". No es posible aprobar cobertura.";
            rejectionReason  = "Garantía vencida el " + warranty.getEndDate();

            // Actualizar estado en BD si aún no está marcada como VENCIDA
            if (warranty.getStatus() != WarrantyStatus.VENCIDA) {
                warranty.setStatus(WarrantyStatus.VENCIDA);
                warrantyRepository.save(warranty);
            }

        } else {
            result           = WarrantyValidationStatus.VIGENTE;
            coverageApproved = true;
            long daysLeft    = today.until(warranty.getEndDate()).getDays();
            message          = "Garantía vigente hasta el " + warranty.getEndDate()
                    + " (" + daysLeft + " días restantes). Cobertura aprobada.";
        }

        // Registrar la validación en el historial de auditoría
        WarrantyValidation validation = new WarrantyValidation();
        validation.setWarranty(warranty);
        validation.setResult(result);
        validation.setCoverageApproved(coverageApproved);
        validation.setMessage(message);
        validation.setRejectionReason(rejectionReason);
        validation.setValidatedBy(validatedBy);
        validationRepository.save(validation);

        log.info("Garantía {} validada por {} — resultado: {}", warrantyId, validatedBy, result);
        return mapValidationToDTO(validation, warranty);
    }

    /**
     * Obtiene el historial de validaciones realizadas sobre una garantía específica,
     * @param warrantyId id de la garantía
     * @return lista de validaciones realizadas sobre la garantía, ordenadas por fecha de validación
     */
    @Transactional(readOnly = true)
    public List<WarrantyValidationResponseDTO> getValidationHistory(Long warrantyId) {
        Warranty warranty = warrantyRepository.findById(warrantyId)
                .orElseThrow(() -> new RuntimeException(
                        "Garantía no encontrada con id: " + warrantyId));
        return validationRepository.findByWarrantyIdOrderByValidatedAtDesc(warrantyId)
                .stream()
                .map(v -> mapValidationToDTO(v, warranty))
                .collect(Collectors.toList());
    }

    private WarrantyResponseDTO mapToDTO(Warranty w) {
        WarrantyResponseDTO dto = new WarrantyResponseDTO();
        dto.setId(w.getId());
        dto.setVoucherNumber(w.getVoucherNumber());
        dto.setWorkOrderId(w.getWorkOrder().getId());
        dto.setWorkOrderNumber(w.getWorkOrder().getNumberorder());
        dto.setClientName(w.getWorkOrder().getClientname());
        dto.setClientIdentification(w.getWorkOrder().getClientidentification());
        dto.setVehiclePlate(w.getWorkOrder().getVehicleplate());
        if (w.getService() != null) {
            dto.setServiceId(w.getService().getId());
            dto.setServiceName(w.getService().getName());
        }
        if (w.getSparePart() != null) {
            dto.setSparePartId(w.getSparePart().getId());
            dto.setSparePartName(w.getSparePart().getName());
            dto.setSparePartReference(w.getSparePart().getReference());
        }
        dto.setStartDate(w.getStartDate());
        dto.setEndDate(w.getEndDate());
        dto.setStatus(w.getStatus());
        dto.setObservations(w.getObservations());
        dto.setClosureReason(w.getClosureReason());
        dto.setClosureDate(w.getClosureDate());
        dto.setClosedBy(w.getClosedBy());
        dto.setVoucherGeneratedAt(w.getVoucherGeneratedAt());
        dto.setVoucherGeneratedBy(w.getVoucherGeneratedBy());
        dto.setCreatedBy(w.getCreatedBy());
        dto.setCreatedAt(w.getCreatedAt());
        dto.setUpdatedBy(w.getUpdatedBy());
        dto.setUpdatedAt(w.getUpdatedAt());
        return dto;
    }

    private WarrantyValidationResponseDTO mapValidationToDTO(
            WarrantyValidation v, Warranty w) {

        WarrantyValidationResponseDTO dto = new WarrantyValidationResponseDTO();
        dto.setValidationId(v.getId());
        dto.setWarrantyId(w.getId());
        dto.setVoucherNumber(w.getVoucherNumber());
        dto.setClientName(w.getWorkOrder().getClientname());
        dto.setVehiclePlate(w.getWorkOrder().getVehicleplate());

        String coverage = w.getService() != null
                ? "Servicio: " + w.getService().getName()
                : w.getSparePart() != null
                        ? "Repuesto: " + w.getSparePart().getName()
                        : "Sin cobertura definida";
        dto.setCoverageDescription(coverage);

        dto.setStartDate(w.getStartDate());
        dto.setEndDate(w.getEndDate());
        dto.setResult(v.getResult());
        dto.setCoverageApproved(v.getCoverageApproved());
        dto.setMessage(v.getMessage());
        dto.setRejectionReason(v.getRejectionReason());
        dto.setValidatedBy(v.getValidatedBy());
        dto.setValidatedAt(v.getValidatedAt());
        return dto;
    }
}