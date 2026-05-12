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
import com.proyecto.TurboMechanics.entity.PayMethod;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.entity.Vehicle;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.enums.MovementType;
import com.proyecto.TurboMechanics.enums.StatusBill;
import com.proyecto.TurboMechanics.repository.BillRepository;
import com.proyecto.TurboMechanics.repository.MovementPayRepository;
import com.proyecto.TurboMechanics.repository.PayMethodRepository;
import com.proyecto.TurboMechanics.repository.UsersRepository;
import com.proyecto.TurboMechanics.repository.VehicleRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository        billRepository;

    private final UsersRepository       usersRepository;

    private final VehicleRepository     vehicleRepository;

    private final PayMethodRepository   payMethodRepository;

    private final WorkOrderRepository   workOrderRepository;

    private final MovementPayRepository movementPayRepository;

    private final PdfGeneratorService   pdfGeneratorService;

    private final NotificationService   notificationService;

    /**
     * generar factura desde la orden de trabajo
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

        PayMethod payMethod = payMethodRepository.findById(request.getPayMethodId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Método de pago no encontrado: " + request.getPayMethodId()));

        BigDecimal subtotal = request.getSubtotal();
        BigDecimal taxes    = subtotal.multiply(BigDecimal.valueOf(0.19));
        BigDecimal total    = subtotal.add(taxes);

        Bill bill = Bill.builder()
            .numBill(generatedNumBill())   
            .workOrder(workOrder)
            .users(users)
            .vehicle(vehicle)
            .payMethod(payMethod)
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
     * asignar cliente  vehiculo a una factura existente
     * @param billId id de la factura
     * @param identification identifacion del cliente
     * @param plate placa del vehiculo
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
     * @param billId id de la factura
     * @param canal canal por donde envia la factura
     */
    public void sendProof(Long billId, String canal) {
        Bill bill = billRepository.findById(billId)
            .orElseThrow(() -> new EntityNotFoundException(
                "Factura no encontrada: " + billId));

        byte[] pdf  = pdfGeneratorService.generatedBillPdf(bill);
        String item = "Comprobante de pago – " + bill.getNumBill();

        if ("EMAIL".equalsIgnoreCase(canal)) {
            notificationService.SentEmail(bill.getUsers().getEmail(), item, pdf);
        } else if ("WHATSAPP".equalsIgnoreCase(canal)) {
            notificationService.enviarWhatsapp(bill.getUsers().getPhone(), pdf);
        } else {
            throw new IllegalArgumentException(
                "Canal no válido: " + canal + ". Use EMAIL o WHATSAPP");
        }
    }

    /**
     * historial de facturas por cliente y placa
     * @param identification identificacion de cliente
     * @param plate placa del cliente
     * @return retorna el historial de la facturas
     */
    public List<Bill> getHistoryByCustomer(Integer identification, String plate) {
        if (plate != null && !plate.isBlank()) {
            return billRepository.findByUsersIdentificationAndVehiclePlate(
                identification, plate.toUpperCase());
        }
        return billRepository.findByUsersIdentification(identification);
    }

    // RF 5.8 – Reporte de facturación por período
    public List<Bill> getReportByPeriod(LocalDate start, LocalDate end) {
        return billRepository.findByDateBetween(start, end);
    }

    // RF 5.7 – Control de caja quincenal
    public CashierResponseDTO getCashierControl(LocalDate start, LocalDate end) {
        List<MovementPay> movements = movementPayRepository.findByDateBetween(
            start.atStartOfDay(),
            end.atTime(23, 59, 59)
        );

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

    // Generador de número de factura
    private String generatedNumBill() {
        long next = billRepository.count() + 1;
        return String.format("FAC-%d-%06d", Year.now().getValue(), next);
    }
}