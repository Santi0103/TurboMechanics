package com.proyecto.TurboMechanics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.VehiculoCliente;

@Repository
public interface VehiculoClienteRepository extends JpaRepository<VehiculoCliente, Long> {

    /**busca todos los vehiculos de un usuario por su id */
    List<VehiculoCliente> findByUsuarioId(Long usuarioId);

    /**busca un vehiculo por placa ignorando mayusculas y minusculas */
    Optional<VehiculoCliente> findByPlacaIgnoreCase(String placa);

    /**verifica si ya existe un vehiculo con esa placa para ese usuario */
    boolean existsByPlacaIgnoreCaseAndUsuarioId(String placa, Long usuarioId);
}