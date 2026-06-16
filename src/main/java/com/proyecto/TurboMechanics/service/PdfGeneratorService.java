package com.proyecto.TurboMechanics.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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

    private static final DeviceRgb PRIMARY   = new DeviceRgb(30, 58, 138);
    private static final DeviceRgb SECONDARY = new DeviceRgb(239, 246, 255);
    private static final DeviceRgb ACCENT    = new DeviceRgb(59, 130, 246);

    public byte[] generatedBillPdf(Bill bill) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter   writer = new PdfWriter(baos);
            PdfDocument pdf    = new PdfDocument(writer);
            Document    doc    = new Document(pdf, PageSize.A4);
            doc.setMargins(36, 48, 36, 48);

            addHeader(doc, bill);

            addSectionTitle(doc, "Datos del Cliente");
            Table clientTable = twoColumnTable();
            addRow(clientTable, "Nombre",    bill.getUsers().getUsername());
            addRow(clientTable, "Documento", String.valueOf(bill.getUsers().getIdentification()));
            doc.add(clientTable);

            spacer(doc);

            addSectionTitle(doc, "Datos del Vehículo");
            Table vehicleTable = twoColumnTable();
            addRow(vehicleTable, "Placa", bill.getVehicle().getPlate());
            doc.add(vehicleTable);

            spacer(doc);

            addSectionTitle(doc, "Detalle de Factura");
            Table detailTable = twoColumnTable();
            addRow(detailTable, "N° Factura",  bill.getNumBill());
            addRow(detailTable, "Fecha",       bill.getDate().format(FMT));
            addRow(detailTable, "Estado",      bill.getStatus().toString());
            addRow(detailTable, "Emitida por", bill.getCreatedBy());
            doc.add(detailTable);

            spacer(doc);

            addSectionTitle(doc, "Resumen de Pago");
            Table amountTable = twoColumnTable();
            addRow(amountTable, "Subtotal",  "$" + bill.getSubtotal());
            addRow(amountTable, "IVA (19%)", "$" + bill.getTaxes());
            doc.add(amountTable);

            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                    .useAllAvailableWidth().setFontSize(12);

            totalTable.addCell(new Cell()
                    .add(new Paragraph("TOTAL").setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY).setPadding(8)
                    .setBorder(new SolidBorder(ACCENT, 0.5f)));
            totalTable.addCell(new Cell()
                    .add(new Paragraph("$" + bill.getTotal()).setBold().setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(PRIMARY).setPadding(8)
                    .setBorder(new SolidBorder(ACCENT, 0.5f)));
            doc.add(totalTable);

            spacer(doc);
            addFooter(doc);

            doc.close();
            log.info("PDF generado para factura {}", bill.getNumBill());
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando PDF para factura {}: {}", bill.getNumBill(), e.getMessage());
            throw new RuntimeException("Error al generar el PDF", e);
        }
    }

    private void addHeader(Document doc, Bill bill) {
        Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth().setBackgroundColor(PRIMARY).setMarginBottom(16);

        header.addCell(new Cell()
                .add(new Paragraph("TURBO MECHANICS")
                        .setFontSize(20).setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("FACTURA DE SERVICIOS")
                        .setFontSize(11).setFontColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("N°: " + bill.getNumBill())
                        .setFontSize(10).setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER).setPadding(14));
        doc.add(header);
    }

    private void addSectionTitle(Document doc, String title) {
        Table bar = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth().setBackgroundColor(PRIMARY).setMarginBottom(0);

        bar.addCell(new Cell()
                .add(new Paragraph(title).setFontSize(10).setBold().setFontColor(ColorConstants.WHITE))
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(8).setPaddingTop(4).setPaddingBottom(4));
        doc.add(bar);
    }

    private Table twoColumnTable() {
        return new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .useAllAvailableWidth().setFontSize(10).setMarginBottom(0);
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setBold())
                .setBackgroundColor(SECONDARY).setPadding(5)
                .setBorder(new SolidBorder(ACCENT, 0.3f)));
        table.addCell(new Cell()
                .add(new Paragraph(value != null ? value : "—"))
                .setPadding(5).setBorder(new SolidBorder(ACCENT, 0.3f)));
    }

    private void addFooter(Document doc) {
        doc.add(new Paragraph(
                "Este documento es una factura oficial de Turbo Mechanics. " +
                "Conserve este comprobante para cualquier reclamación.")
                .setFontSize(8).setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(18));
    }

    private void spacer(Document doc) {
        doc.add(new Paragraph(" ").setMarginBottom(6));
    }
}