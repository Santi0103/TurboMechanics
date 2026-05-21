package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.QualityCheck;
import com.proyecto.TurboMechanics.enums.QualityCheckStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QualityCheckRepository extends JpaRepository<QualityCheck, Long> {

    Optional<QualityCheck> findByWorkOrderId(Long workOrderId);

    boolean existsByWorkOrderId(Long workOrderId);

    List<QualityCheck> findByStatusOrderByCreatedAtDesc(QualityCheckStatus status);
}