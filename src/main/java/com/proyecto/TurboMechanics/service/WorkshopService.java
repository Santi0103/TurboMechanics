package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.WorkshopRequestDTO;
import com.proyecto.TurboMechanics.dto.WorkshopResponseDTO;
import com.proyecto.TurboMechanics.entity.Workshop;
import com.proyecto.TurboMechanics.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    private static final double DEGREES_PER_KM  = 0.009;
    private static final double DEFAULT_RADIUS_KM = 50.0;

    /**
     * Registra un nuevo taller de la franquicia.
     *
     * @param request   datos del taller
     * @param createdBy usuario que realiza el registro
     * @return datos del taller registrado
     */
    @Transactional
    public WorkshopResponseDTO registerWorkshop(WorkshopRequestDTO request, String createdBy) {
        Workshop workshop = new Workshop();
        applyFields(workshop, request);
        workshop.setCreatedBy(createdBy);
        workshopRepository.save(workshop);
        return mapToDTO(workshop, null);
    }

    /**
     * Obtiene una lista de talleres activos, con opción de filtrar por ciudad.
     * @param city filtro opcional por ciudad (si se proporciona, solo se retornan talleres de esa ciudad; si no, se retornan todos los talleres activos)
     * @return lista de talleres activos ordenados por nombre
     */
    @Transactional(readOnly = true)
    public List<WorkshopResponseDTO> getActiveWorkshops(String city) {
        List<Workshop> workshops = (city != null && !city.isBlank())
                ? workshopRepository.findByCityIgnoreCaseAndActiveTrueAndDeletedAtIsNullOrderByNameAsc(city)
                : workshopRepository.findByActiveTrueAndDeletedAtIsNullOrderByNameAsc();
        return workshops.stream().map(w -> mapToDTO(w, null)).collect(Collectors.toList());
    }

    /**
     * Obtiene una lista de talleres cercanos a una ubicación dada, dentro de un radio especificado.
     * @param clientLat latitud de la ubicación del cliente
     * @param clientLng longitud de la ubicación del cliente
     * @param radiusKm radio de búsqueda en kilómetros (opcional, por defecto 50 km)
     * @return lista de talleres activos dentro del radio especificado, ordenados por distancia al cliente
     */
    @Transactional(readOnly = true)
    public List<WorkshopResponseDTO> getWorkshopsNearby(
            Double clientLat, Double clientLng, Double radiusKm) {

        double radius = radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM;
        double delta  = radius * DEGREES_PER_KM;

        return workshopRepository.findActiveInBoundingBox(
                clientLat - delta, clientLat + delta,
                clientLng - delta, clientLng + delta)
                .stream()
                .map(w -> mapToDTO(w, haversineKm(clientLat, clientLng,
                        w.getLatitude(), w.getLongitude())))
                .filter(dto -> dto.getDistanceKm() <= radius)
                .sorted(Comparator.comparingDouble(WorkshopResponseDTO::getDistanceKm))
                .collect(Collectors.toList());
    }

    /**
     * Obtiene los datos de un taller por su id.
     * @param id id del taller
     * @return datos del taller
     */
    @Transactional(readOnly = true)
    public WorkshopResponseDTO getWorkshopById(Long id) {
        return mapToDTO(findById(id), null);
    }

    /**
     * Actualiza los datos de un taller existente. No permite modificar un taller eliminado.
     * @param id id del taller a modificar
     * @param request nuevos datos del taller
     * @param updatedBy usuario que realiza la actualización
     * @return datos del taller actualizado
     */
    @Transactional
    public WorkshopResponseDTO updateWorkshop(Long id, WorkshopRequestDTO request, String updatedBy) {
        Workshop workshop = findById(id);

        if (workshop.getDeletedAt() != null)
            throw new RuntimeException("No se puede modificar un taller eliminado.");

        applyFields(workshop, request);
        workshop.setUpdatedBy(updatedBy);
        // updatedAt se asigna en @PreUpdate
        workshopRepository.save(workshop);
        return mapToDTO(workshop, null);
    }

    /**
     * Realiza un borrado lógico de un taller, marcándolo como inactivo y registrando la fecha y 
     * usuario de eliminación. No permite eliminar un taller ya eliminado.
     * @param id id del taller a eliminar
     * @param deletedBy usuario que realiza la eliminación
     */
    @Transactional
    public void deleteWorkshop(Long id, String deletedBy) {
        Workshop workshop = findById(id);

        if (workshop.getDeletedAt() != null)
            throw new RuntimeException("El taller ya fue eliminado anteriormente.");

        workshop.setActive(false);
        workshop.setDeletedAt(LocalDateTime.now());
        workshop.setDeletedBy(deletedBy);
        workshopRepository.save(workshop);
    }

    private Workshop findById(Long id) {
        return workshopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taller no encontrado con id: " + id));
    }

    private void applyFields(Workshop workshop, WorkshopRequestDTO request) {
        workshop.setName(request.getName());
        workshop.setAddress(request.getAddress());
        workshop.setCity(request.getCity());
        workshop.setState(request.getState());
        workshop.setPhone(request.getPhone());
        workshop.setEmail(request.getEmail());
        workshop.setLatitude(request.getLatitude());
        workshop.setLongitude(request.getLongitude());
        workshop.setSchedule(request.getSchedule());
        if (request.getActive() != null) workshop.setActive(request.getActive());
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    private WorkshopResponseDTO mapToDTO(Workshop w, Double distanceKm) {
        WorkshopResponseDTO dto = new WorkshopResponseDTO();
        dto.setId(w.getId());
        dto.setName(w.getName());
        dto.setAddress(w.getAddress());
        dto.setCity(w.getCity());
        dto.setState(w.getState());
        dto.setPhone(w.getPhone());
        dto.setEmail(w.getEmail());
        dto.setLatitude(w.getLatitude());
        dto.setLongitude(w.getLongitude());
        dto.setSchedule(w.getSchedule());
        dto.setActive(w.getActive());
        dto.setDistanceKm(distanceKm != null
                ? Math.round(distanceKm * 10.0) / 10.0
                : null);
        return dto;
    }
}