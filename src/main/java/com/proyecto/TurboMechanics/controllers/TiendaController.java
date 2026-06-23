package com.proyecto.TurboMechanics.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.TurboMechanics.dto.CreatePaymentResponseDTO;
import com.proyecto.TurboMechanics.dto.SparePartsResponseDTO;
import com.proyecto.TurboMechanics.dto.TiendaCompraRequestDTO;
import com.proyecto.TurboMechanics.entity.SpareParts;
import com.proyecto.TurboMechanics.entity.SpareSale;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.enums.SpareSaleStatus;
import com.proyecto.TurboMechanics.repository.SparePartsRepository;
import com.proyecto.TurboMechanics.repository.SpareSaleRepository;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.MercadoPagoService;
import com.proyecto.TurboMechanics.service.SparePartsService;

import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tienda")
@RequiredArgsConstructor
public class TiendaController {

    private final SparePartsRepository sparePartsRepository;
    private final SpareSaleRepository  spareSaleRepository;
    private final MercadoPagoService   mercadoPagoService;
    private final SparePartsService    sparePartsService;

    /**
     * Endpoint público — lista los repuestos disponibles incluyendo imagen
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
                    .imageUrl(r.getImageUrl())
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
     * Endpoint protegido — compra un repuesto, descuenta stock y registra la venta
     */
    @PostMapping("/comprar")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO, RolEnum.CLIENTE })
    public ResponseEntity<CreatePaymentResponseDTO> comprarRepuesto(
            @Valid @RequestBody TiendaCompraRequestDTO request) {
        try {
            SpareParts sparePart = sparePartsRepository.findById(request.getSparePartId())
                .orElseThrow(() -> new EntityNotFoundException(
                    "Repuesto no encontrado: " + request.getSparePartId()));

            if (sparePart.getStock() <= 0) {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }

            // Descontar stock
            sparePart.setStock(sparePart.getStock() - 1);
            sparePartsRepository.save(sparePart);

            // Registrar la salida en el historial de movimientos de inventario
            // para que quede reflejada en el reporte de "repuestos más usados"
            sparePartsService.registerSaleMovement(sparePart, 1);

            // Crear preferencia en MercadoPago
            CreatePaymentResponseDTO response = mercadoPagoService.createPreferenceForRepuesto(
                sparePart.getName(),
                sparePart.getReference(),
                sparePart.getPrice(),
                request.getPayerEmail(),
                request.getPayerFirstName(),
                request.getPayerLastName()
            );

            // Registrar la venta
            SpareSale sale = SpareSale.builder()
                .sparePart(sparePart)
                .payerEmail(request.getPayerEmail())
                .price(sparePart.getPrice())
                .externalReference(response.getExternalReference())
                .preferenceId(response.getPreferenceId())
                .status(SpareSaleStatus.PENDING)
                .build();
            spareSaleRepository.save(sale);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}