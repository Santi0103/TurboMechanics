package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.WorkEvidence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.proyecto.TurboMechanics.enums.EvidenceType;
import java.util.List;

@Repository
public interface WorkEvidenceRepository extends JpaRepository<WorkEvidence, Long> {

    List<WorkEvidence> findByWorkOrderIdOrderByUploadedAtDesc(Long workOrderId);

    List<WorkEvidence> findByWorkOrderIdAndEvidenceTypeOrderByUploadedAtDesc(
        Long workOrderId, EvidenceType evidenceType);
}