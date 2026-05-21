package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.WarrantyValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WarrantyValidationRepository extends JpaRepository<WarrantyValidation, Long> {

    List<WarrantyValidation> findByWarrantyIdOrderByValidatedAtDesc(Long warrantyId);
}