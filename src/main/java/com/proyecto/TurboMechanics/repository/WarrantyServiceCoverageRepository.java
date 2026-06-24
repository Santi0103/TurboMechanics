package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.WarrantyServiceCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyServiceCoverageRepository extends JpaRepository<WarrantyServiceCoverage, Long> {

    /** Filas de cobertura (de cualquier garantía) que apuntan a este servicio */
    List<WarrantyServiceCoverage> findByService_Id(Long serviceId);
}