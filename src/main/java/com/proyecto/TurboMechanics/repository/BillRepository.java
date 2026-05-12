package com.proyecto.TurboMechanics.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.Bill;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByUsersIdentification(Integer identification);

    List<Bill> findByUsersIdentificationAndVehiclePlate(Integer identification, String plate);

    List<Bill> findByDateBetween(LocalDate start, LocalDate end);

    Optional<Bill> findByNumBill(Long numBill);
}