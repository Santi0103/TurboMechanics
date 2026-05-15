package com.proyecto.TurboMechanics.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.TurboMechanics.dto.CashierResponseDTO;
import com.proyecto.TurboMechanics.dto.GeneratedBillRequestDTO;
import com.proyecto.TurboMechanics.entity.Bill;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.BillService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/facturas")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    /**
     * genera la factura
     * 
     * @param request dto para geenrar la facturar
     * @return retorna la geenracion de la factura
     */
    @PostMapping
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<Bill> generate(@Valid @RequestBody GeneratedBillRequestDTO request) {
        try {
            Bill bill = billService.generateFromWorkOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(bill);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * asignar el cliente y vehiculo a una factura
     * 
     * @param id            id de la factura
     * @param identfication identificacion del cliente
     * @param plate         placa del vehiculo
     * @return retorna la factura con cliente y vehiculo asiganado
     */
    @PatchMapping("/{id}/asignar")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<Bill> assign(@PathVariable Long id, @RequestParam Integer identfication,
            @RequestParam String plate) {
        try {
            Bill bill = billService.assignVehicleCustomer(id, identfication, plate);
            return ResponseEntity.status(HttpStatus.OK).body(bill);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * descargar la factura en pdf
     * 
     * @param id id de la factura
     * @return retorna el pdf de la factura
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable Long id) {
        byte[] pdf = billService.downloadPDF(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"factura-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Enviar el comprobante al cliente
     * 
     * @param id    id del comprobante
     * @param canal canal del envio del comprobante
     * @return retorna el envia el comprobante
     */
    @PostMapping("/{id}/comprobante")
    @RequiresRole({ RolEnum.ADMIN, RolEnum.MECANICO })
    public ResponseEntity<Void> sendProof(@PathVariable Long id, @RequestParam String canal) {
        try {
            billService.sendProof(id, canal);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

    }

    /**
     * historial de facturas por cliente
     * 
     * @param identification identificacion del cliente
     * @param plate          placa del vehiculo
     * @return retorna el historial de facturas
     */
    @GetMapping("/historial")
    @RequiresRole({ RolEnum.ADMIN })
    public ResponseEntity<List<Bill>> history(@RequestParam Integer identification,
            @RequestParam(required = false) String plate) {
        try {
            List<Bill> bill = billService.getHistoryByCustomer(identification, plate);
            return ResponseEntity.status(HttpStatus.OK).body(bill);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Reporte de facturacion por periodo
     * 
     * @param start fecha de incio
     * @param end   fecha de fin
     * @return retorna el reporte de facturacion por periodo
     */
    @GetMapping("/reporte")
    @RequiresRole({ RolEnum.ADMIN })
    public ResponseEntity<List<Bill>> report(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            List<Bill> bill = billService.getReportByPeriod(start, end);
            return ResponseEntity.status(HttpStatus.OK).body(bill);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Control de caja quincenal
     * 
     * @param start fecha de incio
     * @param end   fecha de fin
     * @return retorna el control de caja quincenal
     */
    @GetMapping("/caja")
    @RequiresRole({ RolEnum.ADMIN })
    public ResponseEntity<CashierResponseDTO> cashier(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            CashierResponseDTO response = billService.getCashierControl(start, end);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    /**
     * Exportar control de caja a Excel
     */
    @GetMapping("/caja/export")
    @RequiresRole({ RolEnum.ADMIN })
    public ResponseEntity<byte[]> exportCashier(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        try {
            byte[] excel = billService.exportCashierToExcel(start, end);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"caja-" + start + "-" + end + ".xlsx\"")
                    .contentType(MediaType
                            .parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excel);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
