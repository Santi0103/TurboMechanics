package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.enums.LaborStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.proyecto.TurboMechanics.entity.Mechanic;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MechanicRepository extends JpaRepository<Mechanic, Long> {

    Optional<Mechanic> findByDocument(Long document);

    List<Mechanic> findByLaborStatus(LaborStatus laborStatus);

    List<Mechanic> findByPositionIgnoreCase(String position);

    List<Mechanic> findByPositionIgnoreCaseAndLaborStatus(String position, LaborStatus laborStatus);

    List<Mechanic> findByHireDateGreaterThanEqual(LocalDate hireDate);

    List<Mechanic> findByPositionIgnoreCaseAndLaborStatusAndHireDateGreaterThanEqual(
            String position, LaborStatus laborStatus, LocalDate hireDate);

    boolean existsByDocument(Long document);
}