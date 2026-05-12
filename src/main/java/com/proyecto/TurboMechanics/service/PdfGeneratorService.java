package com.proyecto.TurboMechanics.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.proyecto.TurboMechanics.entity.Bill;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PdfGeneratorService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * genera la factura en pdf
     * 
     * @param bill entidad de la factura
     * @return retorna el pdf de la factura
     */
    public byte[] generatedBillPdf(Bill bill) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            // Encabezado
            doc.add(new Paragraph("FACTURA N°: " + bill.getNumBill())
                .setFontSize(18).setBold());
            doc.add(new Paragraph("Fecha: " + bill.getDate().format(FMT)));

            doc.add(new LineSeparator(new SolidLine()));

            // Datos del cliente y vehículo
            doc.add(new Paragraph("Cliente   : " + bill.getUsers().getUsername()));
            doc.add(new Paragraph("Documento : " + bill.getUsers().getIdentification()));
            doc.add(new Paragraph("Vehículo  : " + bill.getVehicle().getPlate()));

            doc.add(new LineSeparator(new SolidLine()));

            // Montos
            doc.add(new Paragraph("Subtotal  : $" + bill.getSubtotal()));
            doc.add(new Paragraph("IVA (19%) : $" + bill.getTaxes()));
            doc.add(new Paragraph("TOTAL     : $" + bill.getTotal()).setBold());

            doc.add(new LineSeparator(new SolidLine()));

            // Pie
            doc.add(new Paragraph("Estado    : " + bill.getStatus()));
            doc.add(new Paragraph("Emitida por: " + bill.getCreatedBy()));

            doc.close();

            log.info("PDF generado para factura {}", bill.getNumBill());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando PDF para factura {}: {}", bill.getNumBill(), e.getMessage());
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }
}