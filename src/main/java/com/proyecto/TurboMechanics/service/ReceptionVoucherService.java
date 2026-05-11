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
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ReceptionVoucherService {

    private final WorkOrderRepository workOrderRepository;

    private static final DateTimeFormatter DATETIME_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Colores corporativos
    private static final DeviceRgb PRIMARY   = new DeviceRgb(30, 58, 138);   // azul oscuro
    private static final DeviceRgb SECONDARY = new DeviceRgb(239, 246, 255); // azul muy claro
    private static final DeviceRgb ACCENT    = new DeviceRgb(59, 130, 246);  // azul medio

    /**
     * Genera el comprobante de recepción en PDF para la orden con el ID dado.
     * @param orderId ID de la orden de trabajo
     * @return byte[] con el PDF generado
     */
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long orderId) {
        WorkOrder order = workOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada con id: " + orderId));
        return buildPdf(order);
    }

    /**
     * Genera el comprobante de recepción en PDF para la orden con el número dado.
     * @param numberOrder número de orden (ej: OT-2025-0001)
     * @return byte[] con el PDF generado
     */
    @Transactional(readOnly = true)
    public byte[] generatePdfByNumber(String numberOrder) {
        WorkOrder order = workOrderRepository.findByNumberorder(numberOrder)
                .orElseThrow(() -> new RuntimeException("Orden no encontrada: " + numberOrder));
        return buildPdf(order);
    }

    
    private byte[] buildPdf(WorkOrder o) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PdfWriter  writer  = new PdfWriter(out);
            PdfDocument pdf    = new PdfDocument(writer);
            Document   doc     = new Document(pdf, PageSize.A4);
            doc.setMargins(36, 48, 36, 48);

            addHeader(doc, o);

            // ── Sección: Datos del cliente ──────────────────────────────────
            addSectionTitle(doc, "Datos del Cliente");
            Table clientTable = twoColumnTable();
            addRow(clientTable, "Nombre",     o.getClientname());
            addRow(clientTable, "Documento",  o.getClientidentification());
            addRow(clientTable, "Teléfono",   o.getClientphone());
            doc.add(clientTable);

            spacer(doc);

            // ── Sección: Datos del vehículo ─────────────────────────────────
            addSectionTitle(doc, "Datos del Vehículo");
            Table vehicleTable = twoColumnTable();
            addRow(vehicleTable, "Placa",   o.getVehicleplate());
            addRow(vehicleTable, "Marca",   o.getVehiclebrand());
            addRow(vehicleTable, "Modelo",  o.getVehiclemodel());
            addRow(vehicleTable, "Año",     String.valueOf(o.getVehicleyear()));
            if (o.getVehiclecolor() != null && !o.getVehiclecolor().isBlank())
                addRow(vehicleTable, "Color", o.getVehiclecolor());
            if (o.getLevelfuel() != null)
                addRow(vehicleTable, "Nivel de combustible", fuelLabel(o.getLevelfuel()));
            if (o.getStatescratches() != null)
                addRow(vehicleTable, "Estado rayones", condLabel(o.getStatescratches()));
            if (o.getStatedents() != null)
                addRow(vehicleTable, "Estado abolladuras", condLabel(o.getStatedents()));
            doc.add(vehicleTable);

            spacer(doc);

            // ── Sección: Fallas reportadas ───────────────────────────────────
            addSectionTitle(doc, "Fallas Reportadas por el Cliente");
            Paragraph failures = new Paragraph(o.getFailuresreported())
                    .setFontSize(10)
                    .setFontColor(ColorConstants.DARK_GRAY)
                    .setMarginTop(4)
                    .setMarginBottom(8)
                    .setPadding(10)
                    .setBackgroundColor(SECONDARY)
                    .setBorder(new SolidBorder(ACCENT, 0.5f));
            doc.add(failures);

            spacer(doc);

            // ── Sección: Accesorios / observaciones ─────────────────────────
            if (o.getAccessoriesobservations() != null && !o.getAccessoriesobservations().isBlank()) {
                addSectionTitle(doc, "Observaciones de Accesorios");
                Paragraph obs = new Paragraph(o.getAccessoriesobservations())
                        .setFontSize(10)
                        .setFontColor(ColorConstants.DARK_GRAY)
                        .setMarginTop(4)
                        .setMarginBottom(8)
                        .setPadding(10)
                        .setBackgroundColor(SECONDARY)
                        .setBorder(new SolidBorder(ACCENT, 0.5f));
                doc.add(obs);
                spacer(doc);
            }

            // ── Sección: Datos de recepción ──────────────────────────────────
            addSectionTitle(doc, "Datos de Recepción");
            Table receptionTable = twoColumnTable();
            addRow(receptionTable, "Número de orden",      o.getNumberorder());
            addRow(receptionTable, "Fecha de ingreso",     o.getDateentry().format(DATETIME_FMT));
            addRow(receptionTable, "Estado",               stateLabel(o.getStateorder()));
            addRow(receptionTable, "Prioridad",            priorityLabel(o.getPriority()));
            if (o.getDateestimateddelivery() != null)
                addRow(receptionTable, "Entrega estimada", o.getDateestimateddelivery().format(DATE_FMT));
            if (o.getCreatedBy() != null && !o.getCreatedBy().isBlank())
                addRow(receptionTable, "Recibido por",     o.getCreatedBy());
            doc.add(receptionTable);

            spacer(doc);

            // ── Pie de firma ────────────────────────────────────────────────
            addFooter(doc);

            doc.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el comprobante PDF: " + e.getMessage(), e);
        }
    }

    private void addHeader(Document doc, WorkOrder o) {
        // Franja azul de título
        Table header = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setBackgroundColor(PRIMARY)
                .setMarginBottom(16);

        Cell titleCell = new Cell()
                .add(new Paragraph("TURBO MECHANICS")
                        .setFontSize(20)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("COMPROBANTE DE RECEPCIÓN DE VEHÍCULO")
                        .setFontSize(11)
                        .setFontColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Orden: " + o.getNumberorder())
                        .setFontSize(10)
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER)
                .setPadding(14);
        header.addCell(titleCell);
        doc.add(header);
    }

    private void addSectionTitle(Document doc, String title) {
        Table bar = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .setBackgroundColor(PRIMARY)
                .setMarginBottom(0);

        bar.addCell(new Cell()
                .add(new Paragraph(title)
                        .setFontSize(10)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE))
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(8)
                .setPaddingTop(4)
                .setPaddingBottom(4));
        doc.add(bar);
    }

    private Table twoColumnTable() {
        return new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .useAllAvailableWidth()
                .setFontSize(10)
                .setMarginBottom(0);
    }

    private void addRow(Table table, String label, String value) {
        table.addCell(new Cell()
                .add(new Paragraph(label).setBold())
                .setBackgroundColor(SECONDARY)
                .setPadding(5)
                .setBorder(new SolidBorder(ACCENT, 0.3f)));
        table.addCell(new Cell()
                .add(new Paragraph(value != null ? value : "—"))
                .setPadding(5)
                .setBorder(new SolidBorder(ACCENT, 0.3f)));
    }

    private void addFooter(Document doc) {
        // Línea de firma del cliente
        Table sigTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth()
                .setMarginTop(24);

        sigTable.addCell(new Cell()
                .add(new Paragraph("\n\n_______________________________")
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Firma del Cliente")
                        .setFontSize(9)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));

        sigTable.addCell(new Cell()
                .add(new Paragraph("\n\n_______________________________")
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("Firma del Mecánico / Administrador")
                        .setFontSize(9)
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER));

        doc.add(sigTable);

        doc.add(new Paragraph(
                "Este documento es un comprobante de recepción del vehículo en Turbo Mechanics. " +
                "El taller no se hace responsable por objetos de valor dejados en el vehículo.")
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(18));
    }

    private void spacer(Document doc) {
        doc.add(new Paragraph(" ").setMarginBottom(6));
    }

    // -----------------------------------------------------------------------
    // Labels para enums
    // -----------------------------------------------------------------------

    private String fuelLabel(WorkOrder.LevelFuel f) {
        return switch (f) {
            case VACIO         -> "Vacío";
            case UN_CUARTO     -> "1/4";
            case MITAD         -> "1/2";
            case TRES_CUARTOS  -> "3/4";
            case LLENO         -> "Lleno";
        };
    }

    private String condLabel(WorkOrder.StateCondition c) {
        return switch (c) {
            case SIN_NOVEDAD -> "Sin novedad";
            case LEVE        -> "Leve";
            case MODERADO    -> "Moderado";
            case SEVERO      -> "Severo";
        };
    }

    private String stateLabel(WorkOrder.StateOrder s) {
        return switch (s) {
            case RECIBIDO       -> "Recibido";
            case EN_DIAGNOSTICO -> "En diagnóstico";
            case EN_REPARACION  -> "En reparación";
            case LISTO          -> "Listo";
            case ENTREGADO      -> "Entregado";
            case CANCELADO      -> "Cancelado";
        };
    }

    private String priorityLabel(WorkOrder.Priority p) {
        if (p == null) return "Normal";
        return switch (p) {
            case BAJA    -> "Baja";
            case NORMAL  -> "Normal";
            case ALTA    -> "Alta";
            case URGENTE -> "Urgente";
        };
    }
}
