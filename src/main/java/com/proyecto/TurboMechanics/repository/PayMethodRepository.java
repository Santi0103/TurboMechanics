package com.proyecto.TurboMechanics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.PayMethod;

@Repository
public interface PayMethodRepository extends JpaRepository<PayMethod, Long> {

    List<PayMethod> findByActiveTrue();
}