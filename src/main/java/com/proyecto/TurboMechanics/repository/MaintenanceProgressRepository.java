package com.proyecto.TurboMechanics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.MaintenanceProgress;

@Repository
public interface MaintenanceProgressRepository extends JpaRepository<MaintenanceProgress, Long> {

    List<MaintenanceProgress> findByWorkOrderIdOrderByRegisteredAtAsc(Long workOrderId);
}