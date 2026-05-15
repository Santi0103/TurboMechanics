package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.MechanicAbsence;
import com.proyecto.TurboMechanics.enums.AbsenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MechanicAbsenceRepository extends JpaRepository<MechanicAbsence, Long> {

    List<MechanicAbsence> findByMechanicIdOrderByStartDateDesc(Long mechanicId);

    List<MechanicAbsence> findByMechanicIdAndAbsenceTypeOrderByStartDateDesc(
            Long mechanicId, AbsenceType absenceType);

    /**
     * Verifica si el mecánico tiene una ausencia que se solape con el rango de fechas dado.
     * Se usa para validar disponibilidad antes de asignar una orden.
     */
    @Query("SELECT COUNT(a) > 0 FROM MechanicAbsence a " +
           "WHERE a.mechanic.id = :mechanicId " +
           "AND a.startDate <= :endDate " +
           "AND a.endDate >= :startDate")
    boolean existsOverlappingAbsence(
            @Param("mechanicId") Long mechanicId,
            @Param("startDate")  LocalDateTime startDate,
            @Param("endDate")    LocalDateTime endDate);

    /** Lista ausencias de un mecánico dentro de un rango de fechas */
    @Query("SELECT a FROM MechanicAbsence a " +
           "WHERE a.mechanic.id = :mechanicId " +
           "AND a.startDate >= :from " +
           "AND a.endDate <= :to " +
           "ORDER BY a.startDate DESC")
    List<MechanicAbsence> findByMechanicIdAndDateRange(
            @Param("mechanicId") Long mechanicId,
            @Param("from")       LocalDateTime from,
            @Param("to")         LocalDateTime to);
}