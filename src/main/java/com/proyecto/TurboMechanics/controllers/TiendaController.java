package com.proyecto.TurboMechanics.controllers;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.TurboMechanics.dto.CreatePaymentResponseDTO;
import com.proyecto.TurboMechanics.dto.SparePartsResponseDTO;
import com.proyecto.TurboMechanics.dto.TiendaCompraRequestDTO;
import com.proyecto.TurboMechanics.entity.Bill;
import com.proyecto.TurboMechanics.entity.SpareParts;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.enums.StatusBill;
import com.proyecto.TurboMechanics.repository.BillRepository;
import com.proyecto.TurboMechanics.repository.SparePartsRepository;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.MercadoPagoService;
import com.proyecto.TurboMechanics.dto.CreatePaymentRequestDTO;

import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tienda")
@RequiredArgsConstructor
public class TiendaController {

    private final SparePartsRepository sparePartsRepository;
    private final BillRepository       billRepository;
    private final MercadoPagoService   mercadoPagoService;

    /**
     * Endpoint PÚBLICO — cualquier visitante puede ver los repuestos disponibles.
     * No requiere token.
     */
    @GetMapping("/repuestos")
    public ResponseEntity<List<SparePartsResponseDTO>> listarRepuestos() {
        try {
            List<SparePartsResponseDTO> lista = sparePartsRepository.findAll()
                .stream()
                .map(r -> SparePartsResponseDTO.builder()
                    .id(r.getId())
                    .name(r.getName())
                    .reference(r.getReference())
                    .stock(r.getStock())
                    .stockMin(r.getStockMin())
                    .price(r.getPrice())
                    .category(r.getCategory())
                    .statusStock(r.getStock() <= r.getStockMin() ? "CRÍTICO"
                                : r.getStock() == 0           ? "AGOTADO"
                                : "DISPONIBLE")
                    .build())
                .toList();
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint PROTEGIDO — solo clientes, mecánicos y admins logueados pueden comprar.
     * Crea una factura de repuesto y la referencia de pago en MercadoPago.
     */
    @PostMapping("/comprar")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO, RolEnum.CLIENTE })
    public ResponseEntity<CreatePaymentResponseDTO> comprarRepuesto(
            @Valid @RequestBody TiendaCompraRequestDTO request) {
        try {
            // 1. Buscar el repuesto
            SpareParts repuesto = sparePartsRepository.findById(request.getSparePartId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Repuesto no encontrado: " + request.getSparePartId()));

            if (repuesto.getStock() <= 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // 2. Crear una factura de venta de repuesto
            Bill bill = new Bill();
            bill.setNumBill("REP-" + repuesto.getReference() + "-" + System.currentTimeMillis());
            bill.setTotal(repuesto.getPrice());
            bill.setStatus(StatusBill.Pending);
            Bill savedBill = billRepository.save(bill);

            // 3. Crear preferencia de pago en MercadoPago
            CreatePaymentRequestDTO paymentRequest = new CreatePaymentRequestDTO();
            paymentRequest.setBillId(savedBill.getId());
            paymentRequest.setPayerEmail(request.getPayerEmail());
            paymentRequest.setPayerFirstName(request.getPayerFirstName());
            paymentRequest.setPayerLastName(request.getPayerLastName());

            CreatePaymentResponseDTO response = mercadoPagoService.createPreference(paymentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
