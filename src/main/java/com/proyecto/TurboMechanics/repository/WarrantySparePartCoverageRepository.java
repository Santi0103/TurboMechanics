package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.WarrantySparePartCoverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantySparePartCoverageRepository extends JpaRepository<WarrantySparePartCoverage, Long> {

    /** Filas de cobertura (de cualquier garantía) que apuntan a este repuesto */
    List<WarrantySparePartCoverage> findBySparePart_Id(Long sparePartId);
}