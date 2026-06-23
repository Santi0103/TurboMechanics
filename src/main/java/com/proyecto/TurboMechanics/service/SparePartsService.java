package com.proyecto.TurboMechanics.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.stereotype.Service;

import com.proyecto.TurboMechanics.dto.CriticalStockResponseDTO;
import com.proyecto.TurboMechanics.dto.MovementsRequestDTO;
import com.proyecto.TurboMechanics.dto.MovementsResponseDTO;
import com.proyecto.TurboMechanics.dto.PopularSpacePartsResponseDTO;
import com.proyecto.TurboMechanics.dto.SparePartsRequestDTO;
import com.proyecto.TurboMechanics.dto.SparePartsResponseDTO;
import com.proyecto.TurboMechanics.entity.InventoryMovements;
import com.proyecto.TurboMechanics.entity.SpareParts;
import com.proyecto.TurboMechanics.enums.MovementType;
import com.proyecto.TurboMechanics.repository.InventoryMovementsRepository;
import com.proyecto.TurboMechanics.repository.SparePartsRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SparePartsService {

    private final SparePartsRepository sparePartsRepository;

    private final InventoryMovementsRepository inventoryMovementsRepository;
    
    /**
     * Registra un nuevo repuesto en el sistema.
     *
     * @param request datos del repuesto a registrar
     * @return repuesto creado en formato DTO
     */
    @Transactional
    public SparePartsResponseDTO registrerSpareParts(SparePartsRequestDTO request) {
        if (sparePartsRepository.existsByReference(request.getReference())) {
            throw new RuntimeException("Ya existe un repuesto con la referencia: " + request.getReference());
        }
        SpareParts spareParts = SpareParts.builder()
                .name(request.getName())
                .reference(request.getReference())
                .stock(request.getStock())
                .price(request.getPrice())
                .category(request.getCategory())
                .stockMin(request.getStockMin() != null ? request.getStockMin() : 5)
                .imageUrl(request.getImageUrl())
                .build();
 
        SpareParts save = sparePartsRepository.save(spareParts);
 
        if (request.getStock() > 0) {
            registerMovements(save, MovementType.Input, request.getStock(), "Registro inicial de inventario");
        }
 
        return toResponse(save);
    }
    
    /**
     * Obtiene la lista de todos los repuestos registrados.
     *
     * @return lista de repuestos en formato DTO
     */
    @Transactional(readOnly = true)
    public List<SparePartsResponseDTO> allSpareParts() {
        return sparePartsRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }
    
    /**
     * Busca un repuesto por su identificador.
     *
     * @param id identificador del repuesto
     * @return repuesto encontrado en formato DTO
     */
    @Transactional(readOnly = true)
    public SparePartsResponseDTO findBySparePartsById(Long id) {
        return toResponse(findOrThrow(id));
    }
    
    /**
     * Actualiza la información de un repuesto.
     *
     * @param id identificador del repuesto
     * @param request nuevos datos del repuesto
     * @return repuesto actualizado en formato DTO
     */
    @Transactional
    public SparePartsResponseDTO updateSpareParts(Long id, SparePartsRequestDTO request) {
        SpareParts spareParts = findOrThrow(id);

        if (request.getStock() != null) {
            int sparePartsQuantity = request.getStock() - spareParts.getStock();
            if (sparePartsQuantity > 0) {
                registerMovements(spareParts, MovementType.Input, sparePartsQuantity, "Ajuste de inventario");
            } else if (sparePartsQuantity < 0) {
                validStock(spareParts, Math.abs(sparePartsQuantity));
                registerMovements(spareParts, MovementType.Output, Math.abs(sparePartsQuantity), "Ajuste de inventario");
            }
            spareParts.setStock(request.getStock());
        }

        if (request.getImageUrl() != null) {
            spareParts.setImageUrl(request.getImageUrl());
        }
 
        return toResponse(sparePartsRepository.save(spareParts));
    }
    
    /**
     * Elimina un repuesto por su identificador.
     *
     * @param id identificador del repuesto
     */
    @Transactional
    public void deleteSpareParts(Long id) {
        if (!sparePartsRepository.existsById(id)) {
            throw new RuntimeException("Repuesto no encontrado con id: " + id);
        }
        sparePartsRepository.deleteById(id);
    }
    
    /**
     * Registra la salida de inventario generada por la compra de un repuesto
     * en la tienda (cliente, mecánico o admin comprando desde /tienda/comprar).
     * El stock ya debe haber sido descontado antes de llamar este método.
     *
     * @param spareParts repuesto vendido
     * @param quantity   cantidad vendida
     */
    @Transactional
    public void registerSaleMovement(SpareParts spareParts, int quantity) {
        registerMovements(spareParts, MovementType.Output, quantity, "Venta en tienda");
    }

    /**
     * Registra un movimiento manual de inventario (entrada o salida).
     * Ajusta el stock según el tipo de movimiento.
     *
     * @param sparePartsId identificador del repuesto
     * @param request datos del movimiento
     * @return movimiento registrado en formato DTO
     */
    @Transactional
    public MovementsResponseDTO registerMovementManual(Long sparePartsId, MovementsRequestDTO request) {
        SpareParts spareParts = findOrThrow(sparePartsId);
 
        if (request.getType() == MovementType.Output) {
            validStock(spareParts, request.getStock());
            spareParts.setStock(spareParts.getStock() - request.getStock());
        } else {
            spareParts.setStock(spareParts.getStock() + request.getStock());
        }
 
        sparePartsRepository.save(spareParts);
        InventoryMovements mov = registerMovements(
                spareParts, request.getType(), request.getStock(), request.getMotive());
 
        return toMovResponse(mov);
    }
    
    /**
     * Obtiene el historial de movimientos de un repuesto.
     *
     * @param sparePartsId identificador del repuesto
     * @return lista de movimientos en formato DTO
     */
    @Transactional(readOnly = true)
    public List<MovementsResponseDTO> readMovements(Long sparePartsId) {
        findOrThrow(sparePartsId); 
        return inventoryMovementsRepository.findBySpareParts_IdOrderByDateDesc(sparePartsId)
                .stream().map(this::toMovResponse).collect(Collectors.toList());
    }
    
    /**
     * Genera un reporte de los repuestos más utilizados.
     *
     * @return lista de repuestos con mayor número de salidas
     */
    @Transactional(readOnly = true)
    public List<PopularSpacePartsResponseDTO> reportSpacePartsPopular() {
        return sparePartsRepository.findsparePartsPopular().stream()
                .map(row -> {
                    SpareParts r = (SpareParts) row[0];
                    Long total = (Long) row[1];
                    return PopularSpacePartsResponseDTO.builder()
                            .spacePartsId(r.getId())
                            .name(r.getName())
                            .reference(r.getReference())
                            .totalOutput(total)
                            .build();
                }).collect(Collectors.toList());
    }
    
    /**
     * Genera un reporte de repuestos con stock crítico.
     *
     * @return lista de repuestos con bajo inventario o agotados
     */
    @Transactional(readOnly = true)
    public List<CriticalStockResponseDTO> reportStockCritical() {
        return sparePartsRepository.findStockCritical().stream()
                .map(r -> CriticalStockResponseDTO.builder()
                        .spacePartsId(r.getId())
                        .name(r.getName())
                        .reference(r.getReference())
                        .currentStock(r.getStock())
                        .stockMin(r.getStockMin())
                        .status(r.getStock() == 0 ? "AGOTADO" : "BAJO")
                        .build())
                .collect(Collectors.toList());
    }
    
    /**
     * Sube o reemplaza la imagen de un repuesto.
     *
     * @param id   identificador del repuesto
     * @param file archivo de imagen
     * @return repuesto actualizado con la nueva URL de imagen
     */
    @Transactional
    public SparePartsResponseDTO uploadImage(Long id, MultipartFile file) {
        SpareParts spareParts = findOrThrow(id);
        try {
            Path dir = Paths.get("uploads/repuestos");
            Files.createDirectories(dir);

            String extension = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains("."))
                extension = originalName.substring(originalName.lastIndexOf('.'));

            String uniqueName = UUID.randomUUID() + extension;
            Path dest = dir.resolve(uniqueName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            String imageUrl = "http://localhost:9090/files/repuestos/" + uniqueName;
            spareParts.setImageUrl(imageUrl);
            return toResponse(sparePartsRepository.save(spareParts));
        } catch (IOException e) {
            throw new RuntimeException("Error al guardar la imagen del repuesto", e);
        }
    }

    /**
     * Registra un movimiento de inventario para un repuesto.
     *
     * @param spareParts repuesto al que se le aplica el movimiento
     * @param type tipo de movimiento (entrada o salida)
     * @param stock cantidad del movimiento
     * @param motive motivo del movimiento
     * @return entidad InventoryMovements guardada en la base de datos
     */
    private InventoryMovements registerMovements(SpareParts spareParts, MovementType type, int stock, String motive) {
        return inventoryMovementsRepository.save(InventoryMovements.builder()
                .spareParts(spareParts)
                .type(type)
                .stock(stock)
                .motive(motive)
                .build());
    }
    
    /**
     * Valida que el stock disponible sea suficiente para una salida.
     *
     * @param spareParts repuesto a validar
     * @param stockOutput cantidad a descontar
     */
    private void validStock(SpareParts spareParts, int stockOutput) {
        if (spareParts.getStock() < stockOutput) {
            throw new RuntimeException("Stock insuficiente. Disponible: " + spareParts.getStock());
        }
    }
    
    /**
     * Busca un repuesto por su ID o lanza una excepción si no existe.
     *
     * @param id identificador del repuesto
     * @return entidad SpareParts encontrada
     */
    private SpareParts findOrThrow(Long id) {
        return sparePartsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repuesto no encontrado con id: " + id));
    }
    
    /**
     * Convierte una entidad SpareParts a su DTO de respuesta.
     * Incluye el estado del stock calculado (AGOTADO, BAJO, NORMAL).
     *
     * @param r entidad del repuesto
     * @return DTO del repuesto
     */
    private SparePartsResponseDTO toResponse(SpareParts r) {
        String status = r.getStock() == 0 ? "AGOTADO"
                : r.getStock() <= r.getStockMin() ? "BAJO" : "NORMAL";
        return SparePartsResponseDTO.builder()
                .id(r.getId())
                .name(r.getName())
                .reference(r.getReference())
                .stock(r.getStock())
                .stockMin(r.getStockMin())
                .price(r.getPrice())
                .category(r.getCategory())
                .statusStock(status)
                .imageUrl(r.getImageUrl())
                .build();
    }
    
    /**
     * Convierte una entidad InventoryMovements a su DTO de respuesta.
     *
     * @param m entidad del movimiento
     * @return DTO del movimiento
     */
    private MovementsResponseDTO toMovResponse(InventoryMovements m) {
        return MovementsResponseDTO.builder()
                .id(m.getId())
                .spacePartsId(m.getSpareParts().getId())
                .spacePartsName(m.getSpareParts().getName())
                .type(m.getType())
                .stock(m.getStock())
                .date(m.getDate())
                .motive(m.getMotive())
                .build();
    }
}