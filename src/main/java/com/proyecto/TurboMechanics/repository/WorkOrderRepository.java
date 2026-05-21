package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByNumberorder(String numberorder);

    List<WorkOrder> findByVehicleplateIgnoreCase(String vehicleplate);

    List<WorkOrder> findByClientidentification(String clientidentification);

    List<WorkOrder> findByStateorder(WorkOrder.StateOrder stateorder);

    boolean existsByNumberorder(String numberorder);

    @Query("SELECT COUNT(o) FROM WorkOrder o WHERE YEAR(o.datecreation) = :anio")
    long countByAnio(int anio);
    
    List<WorkOrder> findByAssignedMechanicIdOrderByDatecreationDesc(Long mechanicId);

    List<WorkOrder> findByAssignedMechanicIdAndStateorderOrderByDatecreationDesc(Long mechanicId, WorkOrder.StateOrder stateorder);

    Optional<WorkOrder> findFirstByVehicleplateIgnoreCaseAndStateorderNotInOrderByDateentryDesc(String vehicleplate, List<WorkOrder.StateOrder> stateorders);

    /**
     * Retorna órdenes de un mecánico dentro de un rango de fechas de creación.
     */
    @Query("SELECT o FROM WorkOrder o " +
           "WHERE o.assignedMechanicId = :mechanicId " +
           "AND o.datecreation >= :from " +
           "AND o.datecreation <= :to " +
           "ORDER BY o.datecreation DESC")
    List<WorkOrder> findByMechanicIdAndDateRange(
            @Param("mechanicId") Long mechanicId,
            @Param("from")       LocalDateTime from,
            @Param("to")         LocalDateTime to);
    
    /**
     * Cuenta las órdenes activas (RECIBIDO, EN_DIAGNOSTICO, EN_REPARACION) de un mecánico.
     * Permite determinar si tiene capacidad disponible.
     */
    @Query("SELECT COUNT(o) FROM WorkOrder o " +
           "WHERE o.assignedMechanicId = :mechanicId " +
           "AND o.stateorder IN ('RECIBIDO', 'EN_DIAGNOSTICO', 'EN_REPARACION')")
    long countActiveOrdersByMechanic(@Param("mechanicId") Long mechanicId);
}