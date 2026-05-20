package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.Workshop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Long> {

    List<Workshop> findByActiveTrueOrderByNameAsc();

    List<Workshop> findByCityIgnoreCaseAndActiveTrueOrderByNameAsc(String city);

    /**
     * Busca talleres activos dentro de un radio geográfico aproximado (caja delimitadora).
     * Para mayor precisión en producción, se puede usar PostGIS o la fórmula Haversine.
     */
    @Query("SELECT w FROM Workshop w " +
           "WHERE w.active = true " +
           "AND w.latitude  BETWEEN :latMin AND :latMax " +
           "AND w.longitude BETWEEN :lngMin AND :lngMax " +
           "ORDER BY w.name ASC")
    List<Workshop> findActiveInBoundingBox(
            @Param("latMin") Double latMin,
            @Param("latMax") Double latMax,
            @Param("lngMin") Double lngMin,
            @Param("lngMax") Double lngMax);
}