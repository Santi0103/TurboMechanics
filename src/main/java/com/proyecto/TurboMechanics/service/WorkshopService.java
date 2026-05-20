package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.WorkshopResponseDTO;
import com.proyecto.TurboMechanics.entity.Workshop;
import com.proyecto.TurboMechanics.repository.WorkshopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    /** Radio aproximado de búsqueda en grados (≈ 50 km) */
    private static final double DEGREES_PER_KM = 0.009;
    private static final double DEFAULT_RADIUS_KM = 50.0;

    /**
     * Retorna todos los talleres activos, opcionalmente filtrados por ciudad.
     * @param city filtrar por ciudad (opcional)
     * @return lista de talleres activos
     */
    @Transactional(readOnly = true)
    public List<WorkshopResponseDTO> getActiveWorkshops(String city) {
        List<Workshop> workshops = (city != null && !city.isBlank())
                ? workshopRepository.findByCityIgnoreCaseAndActiveTrueOrderByNameAsc(city)
                : workshopRepository.findByActiveTrueOrderByNameAsc();

        return workshops.stream().map(w -> mapToDTO(w, null)).collect(Collectors.toList());
    }

    /**
     * Retorna talleres activos cercanos a la ubicación del cliente
     * @param clientLat  latitud del cliente
     * @param clientLng  longitud del cliente
     * @param radiusKm   radio de búsqueda en km (usa DEFAULT_RADIUS_KM si es null)
     * @return lista de talleres activos ordenados por distancia
     */
    @Transactional(readOnly = true)
    public List<WorkshopResponseDTO> getWorkshopsNearby(
            Double clientLat, Double clientLng, Double radiusKm) {

        double radius = radiusKm != null ? radiusKm : DEFAULT_RADIUS_KM;
        double delta  = radius * DEGREES_PER_KM;

        List<Workshop> candidates = workshopRepository.findActiveInBoundingBox(
                clientLat - delta, clientLat + delta,
                clientLng - delta, clientLng + delta);

        return candidates.stream()
                .map(w -> mapToDTO(w, haversineKm(clientLat, clientLng,
                        w.getLatitude(), w.getLongitude())))
                .filter(dto -> dto.getDistanceKm() <= radius)
                .sorted(Comparator.comparingDouble(WorkshopResponseDTO::getDistanceKm))
                .collect(Collectors.toList());
    }

    /**
     * Retorna el detalle de un taller por su id.
     * @param id id del taller
     * @return datos del taller
     */
    @Transactional(readOnly = true)
    public WorkshopResponseDTO getWorkshopById(Long id) {
        Workshop workshop = workshopRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taller no encontrado con id: " + id));
        return mapToDTO(workshop, null);
    }

    /**
     * Calcula la distancia en kilómetros entre dos coordenadas usando la fórmula Haversine.
     */
    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double R = 6371.0; // Radio de la Tierra en km
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