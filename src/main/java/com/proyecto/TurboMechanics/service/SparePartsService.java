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
import com.proyecto.TurboMechanics.dto.SparePartHistoryCheckResponseDTO;
import com.proyecto.TurboMechanics.dto.SparePartsRequestDTO;
import com.proyecto.TurboMechanics.dto.SparePartsResponseDTO;
import com.proyecto.TurboMechanics.entity.InventoryMovements;
import com.proyecto.TurboMechanics.entity.SpareParts;
import com.proyecto.TurboMechanics.entity.SpareSale;
import com.proyecto.TurboMechanics.entity.Warranty;
import com.proyecto.TurboMechanics.entity.WarrantySparePartCoverage;
import com.proyecto.TurboMechanics.enums.MovementType;
import com.proyecto.TurboMechanics.repository.InventoryMovementsRepository;
import com.proyecto.TurboMechanics.repository.SparePartsRepository;
import com.proyecto.TurboMechanics.repository.SpareSaleRepository;
import com.proyecto.TurboMechanics.repository.WarrantyRepository;
import com.proyecto.TurboMechanics.repository.WarrantySparePartCoverageRepository;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SparePartsService {

    private final SparePartsRepository sparePartsRepository;

    private final InventoryMovementsRepository inventoryMovementsRepository;

    private final SpareSaleRepository spareSaleRepository;

    private final WarrantyRepository warrantyRepository;

    private final WarrantySparePartCoverageRepository warrantySparePartCoverageRepository;
    
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

        if (request.getName() != null) {
            spareParts.setName(request.getName());
        }

        if (request.getReference() != null) {
            if (!request.getReference().equals(spareParts.getReference())
                    && sparePartsRepository.existsByReference(request.getReference())) {
                throw new RuntimeException("Ya existe un repuesto con la referencia: " + request.getReference());
            }
            spareParts.setReference(request.getReference());
        }

        if (request.getCategory() != null) {
            spareParts.setCategory(request.getCategory());
        }

        if (request.getPrice() != null) {
            spareParts.setPrice(request.getPrice());
        }

        if (request.getStockMin() != null) {
            spareParts.setStockMin(request.getStockMin());
        }

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
     * Verifica si un repuesto ya tiene historial asociado (ventas y/o
     * garantías), para poder advertir al usuario antes de eliminarlo.
     * No bloquea nada, solo informa.
     *
     * @param id identificador del repuesto
     */
    public SparePartHistoryCheckResponseDTO checkHistory(Long id) {
        int cantidadVentas = spareSaleRepository.findBySparePart_Id(id).size();
        long cantidadGarantias = warrantySparePartCoverageRepository.findBySparePart_Id(id)
                .stream().map(c -> c.getWarranty().getId()).distinct().count();

        return SparePartHistoryCheckResponseDTO.builder()
                .tieneVentas(cantidadVentas > 0)
                .tieneGarantias(cantidadGarantias > 0)
                .cantidadVentas(cantidadVentas)
                .cantidadGarantias((int) cantidadGarantias)
                .build();
    }

    /**
     * Elimina un repuesto por su identificador.
     * <p>
     * El repuesto se borra físicamente, pero su historial NO se pierde:
     * las ventas (spare_sale) y garantías (garantias) que lo referenciaban
     * quedan con el repuesto en null (se desvinculan) en vez de borrarse.
     * Para que las ventas sigan siendo identificables después de perder la
     * relación, se guarda una copia (snapshot) del nombre/referencia/categoría
     * del repuesto en el momento de la eliminación.
     *
     * @param id identificador del repuesto
     */
    @Transactional
    public void deleteSpareParts(Long id) {
        SpareParts spareParts = sparePartsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repuesto no encontrado con id: " + id));

        // 1) Desvincular las filas de cobertura de garantías que cubren este repuesto
        // (quedan con sparePart = null, pero la garantía y la fila en sí se conservan,
        // guardando una copia de sus datos para que el comprobante de garantía siga
        // mostrando qué repuesto cubría)
        List<WarrantySparePartCoverage> coverages = warrantySparePartCoverageRepository.findBySparePart_Id(id);
        for (WarrantySparePartCoverage coverage : coverages) {
            coverage.setNameSnapshot(spareParts.getName());
            coverage.setReferenceSnapshot(spareParts.getReference());
            coverage.setCategorySnapshot(spareParts.getCategory());
            coverage.setSparePart(null);
        }
        warrantySparePartCoverageRepository.saveAll(coverages);

        // 2) Desvincular las ventas hechas sobre este repuesto (quedan con sparePart = null,
        // guardando una copia de sus datos para que sigan siendo identificables)
        List<SpareSale> sales = spareSaleRepository.findBySparePart_Id(id);
        for (SpareSale sale : sales) {
            sale.setSparePartNameSnapshot(spareParts.getName());
            sale.setSparePartReferenceSnapshot(spareParts.getReference());
            sale.setSparePartCategorySnapshot(spareParts.getCategory());
            sale.setSparePart(null);
        }
        spareSaleRepository.saveAll(sales);

        // 3) Eliminar el repuesto. Los movimientos de inventario (InventoryMovements)
        // se eliminan automáticamente por el cascade = ALL definido en SpareParts.movements
        sparePartsRepository.delete(spareParts);
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