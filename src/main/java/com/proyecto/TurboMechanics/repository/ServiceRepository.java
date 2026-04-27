package com.proyecto.TurboMechanics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.ServiceEntity;

@Repository
public interface ServiceRepository extends JpaRepository<ServiceEntity, Long>{
    boolean existsByName(String nombre);
 
    List<ServiceEntity> findByActiveTrue();
}
