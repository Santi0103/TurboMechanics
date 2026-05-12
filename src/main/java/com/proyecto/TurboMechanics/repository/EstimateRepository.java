package com.proyecto.TurboMechanics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.Estimate;

@Repository
public interface EstimateRepository extends JpaRepository<Estimate, Long> {
    List<Estimate> findByUsersIdentification(Integer identification);
 
    /** Presupuesto ligado a una orden de trabajo */
    Optional<Estimate> findByWorkOrderId(Long workOrderId);
 
    /** Presupuestos de un cliente por documento + placa */
    List<Estimate> findByUsersIdentificationAndVehiclePlate(Integer identification, String plate);
     
}
