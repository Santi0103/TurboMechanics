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

    List<Warranty> findByServiceIdOrderByCreatedAtDesc(Long serviceId);

    List<Warranty> findBySparePartIdOrderByCreatedAtDesc(Long sparePartId);

    List<Warranty> findByStatusOrderByCreatedAtDesc(WarrantyStatus status);

    java.util.Optional<Warranty> findByVoucherNumber(String voucherNumber);

    /**
     * Búsqueda de texto libre sobre nombre de cliente, placa, servicio o repuesto.
     */
    @Query("SELECT w FROM Warranty w " +
           "WHERE LOWER(w.workOrder.clientname) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(w.workOrder.vehicleplate) LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(w.service.name)           LIKE LOWER(CONCAT('%', :text, '%')) " +
           "OR LOWER(w.sparePart.name)         LIKE LOWER(CONCAT('%', :text, '%')) " +
           "ORDER BY w.createdAt DESC")
    List<Warranty> searchByText(@Param("text") String text);

    boolean existsByVoucherNumber(String voucherNumber);
}