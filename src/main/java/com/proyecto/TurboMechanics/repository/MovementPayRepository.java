package com.proyecto.TurboMechanics.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.MovementPay;

@Repository
public interface MovementPayRepository extends JpaRepository<MovementPay, Long> {

    List<MovementPay> findByDateBetween(LocalDateTime start,LocalDateTime end);
}