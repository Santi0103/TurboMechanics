package com.proyecto.TurboMechanics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.InventoryMovements;

@Repository
public interface InventoryMovementsRepository extends JpaRepository<InventoryMovements, Long>{
    List<InventoryMovements> findBySpareParts_IdOrderByDateDesc(Long sparePartsId);
}
