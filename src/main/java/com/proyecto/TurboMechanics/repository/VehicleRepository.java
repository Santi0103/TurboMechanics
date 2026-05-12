package com.proyecto.TurboMechanics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.Vehicle;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    Optional<Vehicle> findByPlateIgnoreCase(String plate);

    List<Vehicle> findByOwnerId(Long ownerId);

    List<Vehicle> findByOwnerIdentification(Integer identification);

    boolean existsByPlateIgnoreCase(String plate);
}
