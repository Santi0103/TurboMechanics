package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.Warranty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.proyecto.TurboMechanics.enums.WarrantyStatus;
import java.util.List;

@Repository
public interface WarrantyRepository extends JpaRepository<Warranty, Long> {

    List<Warranty> findByWorkOrderIdOrderByCreatedAtDesc(Long workOrderId);

    List<Warranty> findByWorkOrderClientidentificationOrderByCreatedAtDesc(String clientIdentification);

    List<Warranty> findByWorkOrderVehicleplateIgnoreCaseOrderByCreatedAtDesc(String vehiclePlate);

    /** Garantías que cubren un servicio específico (puede cubrir varios servicios a la vez) */
    @Query("SELECT DISTINCT c.warranty FROM WarrantyServiceCoverage c WHERE c.service.id = :serviceId")
    List<Warranty> findByServiceId(@Param("serviceId") Long serviceId);

    /** Garantías que cubren un repuesto específico (puede cubrir varios repuestos a la vez) */
    @Query("SELECT DISTINCT c.warranty FROM WarrantySparePartCoverage c WHERE c.sparePart.id = :sparePartId")
    List<Warranty> findBySparePartId(@Param("sparePartId") Long sparePartId);

    /**
     * Busca garantías por nombre o identificación del cliente (coincidencia parcial),
     * para el panel de Historial donde se puede escribir el nombre o la cédula.
     */
    @Query("SELECT w FROM Warranty w " +
           "WHERE LOWER(w.workOrder.clientname) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR w.workOrder.clientidentification LIKE CONCAT('%', :text, '%')")
    List<Warranty> findByClientNameOrIdentification(@Param("text") String text);

    List<Warranty> findByStatusOrderByCreatedAtDesc(WarrantyStatus status);

    java.util.Optional<Warranty> findByVoucherNumber(String voucherNumber);

    /**
     * Búsqueda de texto libre sobre nombre de cliente, placa, servicios o repuestos cubiertos.
     */
    @Query("SELECT DISTINCT w FROM Warranty w " +
           "LEFT JOIN w.serviceCoverages s " +
           "LEFT JOIN w.sparePartCoverages c " +
           "WHERE LOWER(w.workOrder.clientname) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(w.workOrder.vehicleplate) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(s.service.name) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(s.nameSnapshot) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(c.nameSnapshot) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(c.sparePart.name) LIKE LOWER(CONCAT('%', :text, '%'))")
    List<Warranty> searchByText(@Param("text") String text);

    boolean existsByVoucherNumber(String voucherNumber);
}