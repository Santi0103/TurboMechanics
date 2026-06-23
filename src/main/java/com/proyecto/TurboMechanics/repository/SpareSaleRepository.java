package com.proyecto.TurboMechanics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.SpareSale;

@Repository
public interface SpareSaleRepository extends JpaRepository<SpareSale, Long> {
    List<SpareSale> findAllByOrderByCreatedAtDesc();
    Optional<SpareSale> findByExternalReference(String externalReference);
    List<SpareSale> findBySparePart_Id(Long sparePartId);
}