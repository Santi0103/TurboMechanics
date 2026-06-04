package com.proyecto.TurboMechanics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.proyecto.TurboMechanics.entity.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Buscar por referencia externa enviada a MercadoPago */
    Optional<Payment> findByExternalReference(String externalReference);

    /** Buscar por id de pago de MercadoPago (para webhooks) */
    Optional<Payment> findByMpPaymentId(Long mpPaymentId);

    /** Historial de pagos de una factura */
    List<Payment> findByBillId(Long billId);
}