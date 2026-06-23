package com.proyecto.TurboMechanics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.SpareParts;

@Repository
public interface SparePartsRepository extends JpaRepository<SpareParts, Long> {
    boolean existsByReference(String referencia);
 
    @Query("SELECT r FROM SpareParts r WHERE r.stock = 0 OR r.stock <= r.stockMin")
    List<SpareParts> findStockCritical();
 
    @Query("SELECT m.spareParts, SUM(m.stock) as totalSalidas FROM InventoryMovements m WHERE m.type = com.proyecto.TurboMechanics.enums.MovementType.Output GROUP BY m.spareParts ORDER BY totalSalidas DESC")
    List<Object[]> findsparePartsPopular(); 
}
