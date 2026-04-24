package com.proyecto.TurboMechanics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.OrdenTrabajo;

@Repository
public interface OrdenTrabajoRepository extends JpaRepository<OrdenTrabajo, Long> {

    Optional<OrdenTrabajo> findByNumeroOrden(String numeroOrden);

    List<OrdenTrabajo> findByVehiculoPlacaIgnoreCase(String placa);

    List<OrdenTrabajo> findByClienteIdentificacion(String identificacion);

    List<OrdenTrabajo> findByEstadoOrden(OrdenTrabajo.EstadoOrden estadoOrden);

    boolean existsByNumeroOrden(String numeroOrden);

    @Query("SELECT COUNT(o) FROM OrdenTrabajo o WHERE YEAR(o.fechaCreacion) = :anio")
    long countByAnio(int anio);
}
