package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.ReceptionVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class ReceptionVoucherController {

    private final ReceptionVoucherService voucherService;

    /**
     * Genera y descarga el comprobante de recepción en PDF para la orden indicada por ID.
     * @param id ID de la orden de trabajo
     * @return 200 OK con el PDF como attachment, 404 si no existe, 500 si falla la generación
     */
    @GetMapping("/{id}/voucher")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<?> downloadVoucherById(@PathVariable Long id) {
        try {
            byte[] pdf = voucherService.generatePdf(id);
            return buildPdfResponse(pdf, "comprobante-recepcion-" + id + ".pdf");
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponseDTO(msg));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error al generar el comprobante: " + msg));
        }
    }

    /**
     * Genera y descarga el comprobante de recepción en PDF para la orden indicada por número de orden.
     * @param numberorder Número de orden de trabajo
     * @return 200 OK con el PDF como attachment, 404 si no existe, 500 si falla la generación
     */
    @GetMapping("/number/{numberorder}/voucher")
    @RequiresRole({ RolEnum.MECANICO, RolEnum.ADMIN })
    public ResponseEntity<?> downloadVoucherByNumber(@PathVariable String numberorder) {
        try {
            byte[] pdf = voucherService.generatePdfByNumber(numberorder);
            String filename = "comprobante-recepcion-" + numberorder + ".pdf";
            return buildPdfResponse(pdf, filename);
        } catch (RuntimeException e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("no encontrada")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new MessageResponseDTO(msg));
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error al generar el comprobante: " + msg));
        }
    }

    private ResponseEntity<byte[]> buildPdfResponse(byte[] pdf, String filename) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(pdf.length))
                .body(pdf);
    }
}
