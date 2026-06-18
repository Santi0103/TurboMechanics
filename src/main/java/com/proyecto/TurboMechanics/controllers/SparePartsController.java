package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.CriticalStockResponseDTO;
import com.proyecto.TurboMechanics.dto.MovementsRequestDTO;
import com.proyecto.TurboMechanics.dto.MovementsResponseDTO;
import com.proyecto.TurboMechanics.dto.PopularSpacePartsResponseDTO;
import com.proyecto.TurboMechanics.dto.SparePartsRequestDTO;
import com.proyecto.TurboMechanics.dto.SparePartsResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.ExportService;
import com.proyecto.TurboMechanics.service.SparePartsService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/admin/inventario")
@RequiredArgsConstructor
public class SparePartsController {

    private final SparePartsService sparePartsService;

    private final ExportService exportService;

    /**
     * Registro de los repuestos
     * @param request SparePartsRequestDTO dto que pide los datos de los repuestos
     * @return retorna el repuesto registrado
     */
    @RequiresRole({RolEnum.ADMIN})
    @PostMapping
    public ResponseEntity<SparePartsResponseDTO> register(@Valid @RequestBody SparePartsRequestDTO request) {
        try {
            SparePartsResponseDTO response = sparePartsService.registrerSpareParts(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Consultar los repuestos
     * @return retorna todos los repuestos
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping
    public ResponseEntity<List<SparePartsResponseDTO>> readSpareParts() {
        try {
            List<SparePartsResponseDTO> response = sparePartsService.allSpareParts();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Busca los repustos por id
     * @param id id del repuesto
     * @return retorna el repuesto buscado por id
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/{id}")
    public ResponseEntity<SparePartsResponseDTO> findById(@PathVariable Long id) {
        try {
            SparePartsResponseDTO response = sparePartsService.findBySparePartsById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Actualizar el repuesto
     * @param id id del repuesto a actualizar
     * @param request SparePartsRequestDTO dto que pide los datos
     * @return retorna el repuesto actualizado
     */
    @RequiresRole({RolEnum.ADMIN})
    @PutMapping("/{id}")
    public ResponseEntity<SparePartsResponseDTO> update(@PathVariable Long id, @Valid @RequestBody SparePartsRequestDTO request) {
        try {
            SparePartsResponseDTO response = sparePartsService.updateSpareParts(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Elimina el repuesto
     * @param id id del repuesto a eliminar
     * @return retorna el repuesto eliminado
     */
    @RequiresRole({RolEnum.ADMIN})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            sparePartsService.deleteSpareParts(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Registra el movimiento del repuesto
     * @param id id del repuesto
     * @param request MovementsRequestDTO dto que pide los datos
     * @return retorna el movimiento registrado
     */
    @RequiresRole({RolEnum.ADMIN})
    @PostMapping("/{id}/movimientos")
    public ResponseEntity<MovementsResponseDTO> registerMovements(@PathVariable Long id, @Valid @RequestBody MovementsRequestDTO request) {
        try {
            MovementsResponseDTO response = sparePartsService.registerMovementManual(id, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Conusltar los movimientos del repuesto
     * @param id id del repuesto
     * @return retorna una lista del movimiento del repuesto
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/{id}/movimientos")
    public ResponseEntity<List<MovementsResponseDTO>> readMovements(@PathVariable Long id) {
        try {
            List<MovementsResponseDTO> response = sparePartsService.readMovements(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    /**
     * Reporte de los repuestos mas usados
     * @return retorna los repuestos mas usasdos 
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/reportes/mas-usados")
    public ResponseEntity<List<PopularSpacePartsResponseDTO>> mostUsedReports() {
        try {
            List<PopularSpacePartsResponseDTO> response = sparePartsService.reportSpacePartsPopular();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reporte de cnatidad critica de repuestos
     * @return retorna una lista de los repuestos con una cantidad critica
     */
    @RequiresRole({RolEnum.ADMIN, RolEnum.MECANICO})
    @GetMapping("/reportes/stock-critico")
    public ResponseEntity<List<CriticalStockResponseDTO>> criticalStockReport() {
        try {
            List<CriticalStockResponseDTO> response = sparePartsService.reportStockCritical();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reporte del stock en ecxel
     * @return retorna un ecxel de la cantidad de los repuestos
     * @throws IOException si ocurre un error al generar el archivo
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/reportes/stock-critico/excel")
    public ResponseEntity<byte[]> exportStockExcel() throws IOException {
        try {
            List<CriticalStockResponseDTO> data = sparePartsService.reportStockCritical();
            byte[] archivo = exportService.exportCriticalStockExcel(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=stock-critico.xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(archivo);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Reporte del los repuestos mas usados
     * @return retorna el ecxel de los repuestos mas usados
     * @throws IOException si ocurre un error al generar el archivo
     */
    @RequiresRole({RolEnum.ADMIN})
    @GetMapping("/reportes/mas-usados/excel")
    public ResponseEntity<byte[]> exportMostUsedExcel() throws IOException {
        try {
            List<PopularSpacePartsResponseDTO> data = sparePartsService.reportSpacePartsPopular();
            byte[] archivo = exportService.exportSparesPopularExcel(data);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=repuestos-mas-usados.xlsx")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(archivo);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}