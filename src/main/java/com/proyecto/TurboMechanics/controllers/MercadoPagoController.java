package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.TurboMechanics.dto.CreatePaymentRequestDTO;
import com.proyecto.TurboMechanics.dto.CreatePaymentResponseDTO;
import com.proyecto.TurboMechanics.dto.MercadoPagoWebhookDTO;
import com.proyecto.TurboMechanics.entity.Payment;
import com.proyecto.TurboMechanics.repository.PaymentRepository;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.service.MercadoPagoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    private final PaymentRepository  paymentRepository;

   
    /**
     * Crear la referencia de pago
     * @param request dto para crear la referencia de pago
     * @return retorna la referencia de pago creada
     */
    @PostMapping
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO, RolEnum.CLIENTE })
    public ResponseEntity<CreatePaymentResponseDTO> createPayment(@Valid @RequestBody CreatePaymentRequestDTO request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(mercadoPagoService.createPreference(request));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * procesar el webhook
     * @param webhook dto para el proceso del webhook
     * @return retorna el proceso del webhook
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(@RequestBody MercadoPagoWebhookDTO webhook) {
        try {
            mercadoPagoService.processWebhook(webhook);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Consulta de pago 
     * @param id id del pago
     * @return retorna la consulta del pago
     */
    @GetMapping("/{id}")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO, RolEnum.CLIENTE })
    public ResponseEntity<Payment> getPayment(@PathVariable Long id) {
        try {
            return paymentRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * historial de pagos de una factura
     * @param billId id de la factura
     * @return retorna el historial de pagos
     */
    @GetMapping("/bill/{billId}")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<List<Payment>> getByBill(@PathVariable Long billId) {
        try {
            List<Payment> payments = mercadoPagoService.getPaymentsByBill(billId);
            return ResponseEntity.status(HttpStatus.OK).body(payments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Verificar estado en mercado pago
     * @param id id del pago
     * @return retorna el estado del pago
     */
    @GetMapping("/{id}/check")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO, RolEnum.CLIENTE })
    public ResponseEntity<String> checkStatus(@PathVariable Long id) {
        try {
           return paymentRepository.findById(id)
            .map(p -> {
                if (p.getMpPaymentId() == null)
                    return ResponseEntity.ok("PENDING");
                return ResponseEntity.ok(
                    mercadoPagoService.checkPaymentStatus(p.getMpPaymentId()).name());
            })
            .orElse(ResponseEntity.notFound().build()); 
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}