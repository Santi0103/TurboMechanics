package com.proyecto.TurboMechanics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.proyecto.TurboMechanics.dto.WorkOrderRequestDTO;
import com.proyecto.TurboMechanics.dto.WorkOrderResponseDTO;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Service
@Validated
@RequiredArgsConstructor
public class WorkOrderService {
    
    /** Repositorio para la entidad WorkOrder */
    private final WorkOrderRepository ordenTrabajoRepository;

    /**
     * Crea una nueva orden de trabajo a partir de los datos recibidos en el DTO de solicitud, guardándola en la base de datos y devolviendo un DTO de respuesta con los datos de la orden creada.
     * @param request DTO con los datos necesarios para crear la orden de trabajo, validado automáticamente por Spring
     * @return DTO con los datos de la orden de trabajo creada, incluyendo el número de orden generado automáticamente
     */
    @Transactional
    public WorkOrderResponseDTO create(@Valid WorkOrderRequestDTO request) {

        WorkOrder orden = new WorkOrder();
        orden.setClientname(request.getClientname());
        orden.setClientidentification(request.getClientidentification());
        orden.setClientphone(request.getClientphone());
        orden.setVehicleplate(request.getVehicleplate().toUpperCase().trim());
        orden.setVehiclebrand(request.getVehiclebrand());
        orden.setVehiclemodel(request.getVehiclemodel());
        orden.setVehicleyear(request.getVehicleyear());
        orden.setVehiclecolor(request.getVehiclecolor());
        orden.setFailuresreported(request.getFailuresreported());
        orden.setDateestimateddelivery(request.getDateestimateddelivery());
        orden.setLevelfuel(request.getLevelfuel());
        orden.setStatescratches(request.getStatescratches());
        orden.setStatedents(request.getStatedents());
        orden.setAccessoriesobservations(request.getAccessoriesobservations());
        orden.setPriority(request.getPriority() != null ? request.getPriority() : WorkOrder.Priority.NORMAL);
        orden.setCreatedBy(request.getCreatedBy());
        orden.setNumberorder(generateOrderNumber());

        WorkOrder guardada = ordenTrabajoRepository.save(orden);
        return toResponse(guardada);
    }

    /**
     * Lista todas las órdenes de trabajo existentes en la base de datos, transformándolas a DTOs de respuesta.
     * @return Lista de WorkOrderResponseDTO con los datos de las órdenes de trabajo
     */
    @Transactional(readOnly = true)
    public List<WorkOrderResponseDTO> list() {
        return ordenTrabajoRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Obtiene una orden de trabajo por su ID, lanzando una excepción si no se encuentra.
     * @param id El ID de la orden a buscar
     * @return WorkOrderResponseDTO con los datos de la orden de trabajo
     */
    @Transactional(readOnly = true)
    public WorkOrderResponseDTO getById(Long id) {
        WorkOrder orden = ordenTrabajoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con id: " + id));
        return toResponse(orden);
    }

    /**
     * Obtiene una orden de trabajo por su número de orden, lanzando una excepción si no se encuentra.
     * @param numeroOrden El número de orden a buscar
     * @return WorkOrderResponseDTO con los datos de la orden de trabajo encontrada, o una excepción si no se encuentra ninguna orden con ese número
     */
    @Transactional(readOnly = true)
    public WorkOrderResponseDTO getByNumber(String numeroOrden) {
        WorkOrder orden = ordenTrabajoRepository.findByNumberorder(numeroOrden)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + numeroOrden));
        return toResponse(orden);
    }

    /**
     * Lista las órdenes de trabajo que coinciden con la placa del vehículo proporcionada, transformándolas a DTOs de respuesta. La búsqueda es insensible a mayúsculas y espacios.
     * @param placa La placa del vehículo a buscar
     * @return Lista de WorkOrderResponseDTO con los datos de las órdenes de trabajo que coinciden con la placa, o una lista vacía si no se encuentran coincidencias
     */
    @Transactional(readOnly = true)
    public List<WorkOrderResponseDTO> listByPlate(String placa) {
        return ordenTrabajoRepository.findByVehicleplateIgnoreCase(placa)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Lista las órdenes de trabajo que coinciden con la identificación del cliente proporcionada, transformándolas a DTOs de respuesta. La búsqueda es insensible a mayúsculas y espacios.
     * @param identificacion La identificación del cliente a buscar
     * @return Lista de WorkOrderResponseDTO con los datos de las órdenes de trabajo que coinciden con la identificación, o una lista vacía si no se encuentran coincidencias
     */
    @Transactional(readOnly = true)
    public List<WorkOrderResponseDTO> listByClient(Integer identificacion) {
        return ordenTrabajoRepository.findByClientidentification(identificacion)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Lista las órdenes de trabajo que coinciden con el estado proporcionado, transformándolas a DTOs de respuesta.
     * @param stateorder El estado de la orden a buscar (RECIBIDO, EN_DIAGNOSTICO, EN_REPARACION, LISTO, ENTREGADO, CANCELADO)
     * @return Lista de WorkOrderResponseDTO con los datos de las órdenes que coinciden con el estado, o lista vacía si no hay coincidencias
     */
    @Transactional(readOnly = true)
    public List<WorkOrderResponseDTO> listByState(WorkOrder.StateOrder stateorder) {
        return ordenTrabajoRepository.findByStateorder(stateorder)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Genera un número de orden único en formato "OT-AAAA-NNNN", donde "AAAA" es el año actual y "NNNN" es un número secuencial que se reinicia cada año. El método verifica que el número generado no exista ya en la base de datos, incrementando el número secuencial hasta encontrar uno disponible.
     * @return Un número de orden único para la nueva orden de trabajo
     */
    private String generateOrderNumber() {
        int anio = LocalDateTime.now().getYear();
        long cantidad = ordenTrabajoRepository.countByAnio(anio) + 1;
        String numero = String.format("OT-%d-%04d", anio, cantidad);
        while (ordenTrabajoRepository.existsByNumberorder(numero)) {
            cantidad++;
            numero = String.format("OT-%d-%04d", anio, cantidad);
        }
        return numero;
    }

    /**
     * Transforma una entidad WorkOrder a un DTO de respuesta.
     * @param o La entidad WorkOrder a transformar
     * @return WorkOrderResponseDTO con los datos de la orden de trabajo
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
        dto.setFailuresreported(o.getFailuresreported());
        dto.setDateentry(o.getDateentry());
        dto.setDateestimateddelivery(o.getDateestimateddelivery());
        dto.setLevelfuel(o.getLevelfuel());
        dto.setStatescratches(o.getStatescratches());
        dto.setStatedents(o.getStatedents());
        dto.setAccessoriesobservations(o.getAccessoriesobservations());
        dto.setStateorder(o.getStateorder());
        dto.setPriority(o.getPriority());
        dto.setCreatedBy(o.getCreatedBy());
        dto.setDatecreation(o.getDatecreation());
        return dto;
    }
}
