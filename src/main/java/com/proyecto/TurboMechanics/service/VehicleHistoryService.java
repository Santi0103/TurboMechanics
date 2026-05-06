package com.proyecto.TurboMechanics.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.proyecto.TurboMechanics.dto.VehicleHistoryResponseDTO;
import com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VehicleHistoryService {
    
    private final WorkOrderRepository workOrderRepository;

    /**
     * busca el historial de un vehículo por PLACA o por IDENTIFICACIÓN DEL CLIENTE.
     */
    private static final String NO_HISTORY_MSG =
        "No se encontraron servicios previos para este vehículo.";

    /** busca el historial de un vehículo por PLACA
     * @param plate placa del vehículo a consultar
     * @return VehicleHistoryResponseDTO con el historial o mensaje vacío
     */
    @Transactional(readOnly = true)
    public VehicleHistoryResponseDTO getHistoryByPlate(String plate) {
        List<WorkOrder> orders = workOrderRepository
                .findByVehicleplateIgnoreCase(plate.trim().toUpperCase());
        return buildResponse(plate.trim().toUpperCase(), orders);
    }

    /**
     * busca el historial de un vehículo por IDENTIFICACIÓN DEL CLIENTE
     * @param clientIdentification
     * @return
     */
    @Transactional(readOnly = true)
    public VehicleHistoryResponseDTO getHistoryByClientIdentification(String clientIdentification) {
        List<WorkOrder> orders = workOrderRepository
                .findByClientidentification(clientIdentification.trim());
        return buildResponse(null, orders);
    }

    /**
     * Construye la respuesta DTO a partir de la lista de órdenes encontradas.
     * @param plate placa del vehículo (puede ser null si se busca por identificación del cliente)
     * @param orders lista de órdenes de trabajo encontradas para el vehículo
     * @return VehicleHistoryResponseDTO con los datos del vehículo, total de servicios, mensaje y detalle del historial
     */
    private VehicleHistoryResponseDTO buildResponse(String plate, List<WorkOrder> orders) {
        VehicleHistoryResponseDTO response = new VehicleHistoryResponseDTO();
        response.setVehicleplate(plate);
        response.setTotalServices(orders.size());

        if (orders.isEmpty()) {
            response.setMessage(NO_HISTORY_MSG);
            response.setHistory(List.of());
            return response;
        }

        WorkOrder first = orders.get(0);
        response.setVehicleplate(first.getVehicleplate());
        response.setVehiclebrand(first.getVehiclebrand());
        response.setVehiclemodel(first.getVehiclemodel());
        response.setVehicleyear(first.getVehicleyear());
        response.setMessage("Se encontraron " + orders.size() + " servicio(s) para este vehículo.");

        List<WorkOrderResponseDTO> history = orders.stream()
                .sorted((a, b) -> b.getDateentry().compareTo(a.getDateentry()))
                .map(this::toResponse)
                .toList();

        response.setHistory(history);
        return response;
    }

   /**
    * Convierte una entidad WorkOrder a un DTO WorkOrderResponseDTO para incluir en el historial del vehículo.
    * @param o orden de trabajo a convertir
    * @return WorkOrderResponseDTO con los datos relevantes de la orden de trabajo para el historial del vehículo
    */
    private WorkOrderResponseDTO toResponse(WorkOrder o) {
        WorkOrderResponseDTO dto = new WorkOrderResponseDTO();
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
        dto.setFailuresreported(o.getFailuresreported());       // tipo de servicio/reparación
        dto.setDateentry(o.getDateentry());                     // fecha de ingreso
        dto.setDateestimateddelivery(o.getDateestimateddelivery());
        dto.setLevelfuel(o.getLevelfuel());
        dto.setStatescratches(o.getStatescratches());
        dto.setStatedents(o.getStatedents());
        dto.setAccessoriesobservations(o.getAccessoriesobservations()); // repuestos/accesorios
        dto.setStateorder(o.getStateorder());                   // estado final
        dto.setPriority(o.getPriority());
        dto.setCreatedBy(o.getCreatedBy());
        dto.setDatecreation(o.getDatecreation());
        return dto;
    }
}
