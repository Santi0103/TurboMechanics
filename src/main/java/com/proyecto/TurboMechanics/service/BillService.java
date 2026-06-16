package com.proyecto.TurboMechanics.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.CashierResponseDTO;
import com.proyecto.TurboMechanics.dto.GeneratedBillRequestDTO;
import com.proyecto.TurboMechanics.entity.Bill;
import com.proyecto.TurboMechanics.entity.MovementPay;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.entity.Vehicle;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.enums.MovementType;
import com.proyecto.TurboMechanics.enums.StatusBill;
import com.proyecto.TurboMechanics.repository.BillRepository;
import com.proyecto.TurboMechanics.repository.MovementPayRepository;
import com.proyecto.TurboMechanics.repository.UsersRepository;
import com.proyecto.TurboMechanics.repository.VehicleRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;

    private final UsersRepository usersRepository;

    private final VehicleRepository vehicleRepository;

    private final WorkOrderRepository workOrderRepository;

    private final MovementPayRepository movementPayRepository;

    private final PdfGeneratorService pdfGeneratorService;

    private final NotificationService notificationService;

    /**
     * generar factura desde la orden de trabajo
     * 
     * @param request dto para generar la factura
     * @return retorna la factura creada
     */
    @Transactional
    public Bill generateFromWorkOrder(GeneratedBillRequestDTO request) {

        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderID())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Orden de trabajo no encontrada: " + request.getWorkOrderID()));

        Users users = usersRepository.findByIdentification(request.getIdentification())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente no encontrado con documento: " + request.getIdentification()));

        Vehicle vehicle = vehicleRepository.findByPlateIgnoreCase(request.getPlate())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Vehículo no encontrado con placa: " + request.getPlate()));

        BigDecimal subtotal = request.getSubtotal();
        BigDecimal taxes = subtotal.multiply(BigDecimal.valueOf(0.19));
        BigDecimal total = subtotal.add(taxes);

        Bill bill = Bill.builder()
                .numBill(generatedNumBill())
                .workOrder(workOrder)
                .users(users)
                .vehicle(vehicle)
                /**.payMethod(payMethod)*/
                .date(LocalDate.now())
                .subtotal(subtotal)
                .taxes(taxes)
                .total(total)
                .status(StatusBill.Pending)
                .createdBy(request.getCreatedBy())
                .build();

        return billRepository.save(bill);
    }

    /**
     * asignar cliente vehiculo a una factura existente
     * 
     * @param billId         id de la factura
     * @param identification identifacion del cliente
     * @param plate          placa del vehiculo
     * @return retorna la asisgnacion del cliente y vehiculo a la factura
     */
    @Transactional
    public Bill assignVehicleCustomer(Long billId, Integer identification, String plate) {

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada: " + billId));

        Users users = usersRepository.findByIdentification(identification)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Cliente no encontrado con documento: " + identification));

        Vehicle vehicle = vehicleRepository.findByPlateIgnoreCase(plate)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Vehículo no encontrado con placa: " + plate));

        bill.setUsers(users);
        bill.setVehicle(vehicle);
        return billRepository.save(bill);
    }

    /**
     * descargar la factura a pdf
     * 
     * @param billId id de la factura
     * @return retorna el pdf de la factura
     */
    public byte[] downloadPDF(Long billId) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada: " + billId));
        return pdfGeneratorService.generatedBillPdf(bill);
    }

    /**
     * enviar comprobante de pago al cliente
     * 
     * @param billId id de la factura
     * @param canal  canal por donde envia la factura
     */
    public void sendProof(Long billId, String canal) {
        Bill bill = billRepository.findById(billId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Factura no encontrada: " + billId));

        byte[] pdf = pdfGeneratorService.generatedBillPdf(bill);
        String item = "Comprobante de pago – " + bill.getNumBill();

        if ("EMAIL".equalsIgnoreCase(canal)) {
            notificationService.SendEmail(bill.getUsers().getEmail(), item, pdf);
        } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
            notificationService.sendWhatsapp(bill.getUsers().getPhone(), pdf);
        } else {
            throw new IllegalArgumentException(
                    "Canal no válido: " + canal + ". Use EMAIL o WHATSAPP");
        }
    }

    /**
     * historial de facturas por cliente y placa
     * 
     * @param identification identificacion de cliente
     * @param plate          placa del cliente
     * @return retorna el historial de la facturas
     */
    public List<Bill> getHistoryByCustomer(Integer identification, String plate) {
        if (plate != null && !plate.isBlank()) {
            return billRepository.findByUsersIdentificationAndVehiclePlate(
                    identification, plate.toUpperCase());
        }
        return billRepository.findByUsersIdentification(identification);
    }

    /**
     * Reporte de facturación por período.
     * 
     * @param start fecha de inicio
     * @param end   fecha de fin
     * @return retorna la lista de facturas del período
     */
    public List<Bill> getReportByPeriod(LocalDate start, LocalDate end) {
        return billRepository.findByDateBetween(start, end);
    }

    /**
     * Control de caja quincenal.
     * 
     * @param start fecha de inicio
     * @param end   fecha de fin
     * @return retorna el control de caja
     */
    public CashierResponseDTO getCashierControl(LocalDate start, LocalDate end) {
        List<MovementPay> movements = movementPayRepository.findByDateBetween(
                start.atStartOfDay(),
                end.atTime(23, 59, 59));

        BigDecimal inputs = movements.stream()
                .filter(m -> m.getType() == MovementType.Input)
                .map(MovementPay::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outputs = movements.stream()
                .filter(m -> m.getType() == MovementType.Output)
                .map(MovementPay::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CashierResponseDTO(start, end, inputs, outputs, inputs.subtract(outputs));
    }

    /**
     * Generar número de factura.
     * 
     * @return retorna el número de factura generado
     */
    private String generatedNumBill() {
        long next = billRepository.count() + 1;
        return String.format("FAC-%d-%06d", Year.now().getValue(), next);
    }

    /**
     * Exportar el control de caja a un archivo Excel.
     * 
     * @param start fecha de inicio
     * @param end   fecha de fin
     * @return retorna el archivo Excel con el control de caja
     */
    public byte[] exportCashierToExcel(LocalDate start, LocalDate end) {
        try {
            CashierResponseDTO cashier = getCashierControl(start, end);
            List<MovementPay> movements = movementPayRepository
                    .findByDateBetween(start.atStartOfDay(), end.plusDays(1).atStartOfDay());

            org.apache.poi.xssf.usermodel.XSSFWorkbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();

            // ── Hoja 1: Resumen ───────────────────────────────────────────────
            org.apache.poi.ss.usermodel.Sheet summary = workbook.createSheet("Resumen");

            org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            org.apache.poi.ss.usermodel.Row titleRow = summary.createRow(0);
            titleRow.createCell(0).setCellValue("TURBO MECHANICS - CONTROL DE CAJA");
            titleRow.getCell(0).setCellStyle(headerStyle);

            summary.createRow(1).createCell(0).setCellValue(
                    "Período: " + start + " al " + end);

            summary.createRow(2);

            org.apache.poi.ss.usermodel.Row headerRow = summary.createRow(3);
            headerRow.createCell(0).setCellValue("Concepto");
            headerRow.createCell(1).setCellValue("Valor");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerRow.getCell(1).setCellStyle(headerStyle);

            summary.createRow(4).createCell(0).setCellValue("Ingresos");
            summary.getRow(4).createCell(1).setCellValue(cashier.getInputs().doubleValue());

            summary.createRow(5).createCell(0).setCellValue("Egresos");
            summary.getRow(5).createCell(1).setCellValue(cashier.getOutputs().doubleValue());

            summary.createRow(6).createCell(0).setCellValue("Balance");
            summary.getRow(6).createCell(1).setCellValue(cashier.getBalance().doubleValue());

            summary.setColumnWidth(0, 5000);
            summary.setColumnWidth(1, 4000);

            // ── Hoja 2: Detalle movimientos ───────────────────────────────────
            org.apache.poi.ss.usermodel.Sheet detail = workbook.createSheet("Movimientos");

            org.apache.poi.ss.usermodel.Row dHeader = detail.createRow(0);
            dHeader.createCell(0).setCellValue("Fecha");
            dHeader.createCell(1).setCellValue("Tipo");
            dHeader.createCell(2).setCellValue("Concepto");
            dHeader.createCell(3).setCellValue("Descripción");
            dHeader.createCell(4).setCellValue("Monto");
            dHeader.createCell(5).setCellValue("Método de pago");
            for (int i = 0; i <= 5; i++)
                dHeader.getCell(i).setCellStyle(headerStyle);

            int rowNum = 1;
            for (MovementPay m : movements) {
                org.apache.poi.ss.usermodel.Row row = detail.createRow(rowNum++);
                row.createCell(0).setCellValue(m.getDate() != null ? m.getDate().toString() : "");
                row.createCell(1).setCellValue(m.getType() != null ? m.getType().toString() : "");
                row.createCell(2).setCellValue(m.getConcept() != null ? m.getConcept().toString() : "");
                row.createCell(3).setCellValue(m.getDescription() != null ? m.getDescription() : "");
                row.createCell(4).setCellValue(m.getAmount() != null ? m.getAmount().doubleValue() : 0);
            }

            detail.setColumnWidth(0, 5000);
            detail.setColumnWidth(1, 3000);
            detail.setColumnWidth(2, 3500);
            detail.setColumnWidth(3, 7000);
            detail.setColumnWidth(4, 3500);
            detail.setColumnWidth(5, 4000);

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al exportar Excel", e);
        }
    }
}