package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    List<Diagnosis> findByWorkOrderIdOrderByRegistrationdateDesc(Long workOrderId);

    boolean existsByWorkOrderId(Long workOrderId);
}
