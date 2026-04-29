package com.proyecto.TurboMechanics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.WorkOrder;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {

    Optional<WorkOrder> findByNumberorder(String numberorder);

    List<WorkOrder> findByVehicleplateIgnoreCase(String vehicleplate);

    List<WorkOrder> findByClientidentification(String clientidentification);

    List<WorkOrder> findByStateorder(WorkOrder.StateOrder stateorder);

    boolean existsByNumberorder(String numberorder);

    @Query("SELECT COUNT(o) FROM WorkOrder o WHERE YEAR(o.datecreation) = :anio")
    long countByAnio(int anio);
}
