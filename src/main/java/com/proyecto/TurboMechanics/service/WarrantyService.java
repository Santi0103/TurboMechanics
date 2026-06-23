package com.proyecto.TurboMechanics.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.proyecto.TurboMechanics.dto.CloseWarrantyRequestDTO;
import com.proyecto.TurboMechanics.dto.ServiceCoverageItemDTO;
import com.proyecto.TurboMechanics.dto.SparePartCoverageItemDTO;
import com.proyecto.TurboMechanics.dto.WarrantyRequestDTO;
import com.proyecto.TurboMechanics.dto.WarrantyResponseDTO;
import com.proyecto.TurboMechanics.entity.*;
import com.proyecto.TurboMechanics.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.proyecto.TurboMechanics.enums.WarrantyStatus;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarrantyService {

    private final WarrantyRepository    warrantyRepository;
    private final WorkOrderRepository   workOrderRepository;
    private final ServiceRepository     serviceRepository;
    private final SparePartsRepository  sparePartsRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Registra una nueva garantía para una orden de trabajo existente.
     * Una sola garantía puede cubrir varios servicios y/o varios repuestos a la vez.
     * @param request datos de la garantía a registrar
     * @param createdBy usuario que crea la garantía
     * @return datos de la garantía registrada
     */
    @Transactional
    public WarrantyResponseDTO registerWarranty(WarrantyRequestDTO request, String createdBy) {

        WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                .orElseThrow(() -> new RuntimeException(
                        "Orden de trabajo no encontrada con id: " + request.getWorkOrderId()));

        if (request.getEndDate().isBefore(request.getStartDate()))
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio.");

        Warranty warranty = new Warranty();
        warranty.setWorkOrder(workOrder);
        warranty.setStartDate(request.getStartDate());
        warranty.setEndDate(request.getEndDate());
        warranty.setObservations(request.getObservations());
        warranty.setCreatedBy(createdBy);

        applyServices(warranty, request.getServiceIds());
        applySpareParts(warranty, request.getSparePartIds());

        // Estado inicial según vigencia
        warranty.setStatus(resolveStatus(warranty));

        warrantyRepository.save(warranty);
        log.info("Garantía registrada con id {} por {}", warranty.getId(), createdBy);
        return mapToDTO(warranty);
    }

    /**
     * Retorna todas las garantías registradas, actualizando su estado según la fecha actual.
     * @return lista de garantías con su estado actualizado
     */
    @Transactional
    public List<WarrantyResponseDTO> getAllWarranties() {
        return warrantyRepository.findAll().stream()
                .map(this::refreshStatusAndSave)
                .filter(w -> w.getStatus() != WarrantyStatus.CERRADA) // cerradas solo viven en el Historial
                .map(this::mapToDTO)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Filtra garantías por identificación del cliente.
     * @param clientIdentification documento de identificación del cliente
     * @return lista de garantías asociadas al cliente
     */
    @Transactional
    public List<WarrantyResponseDTO> getWarrantiesByClient(String clientIdentification) {
        return warrantyRepository
                .findByWorkOrderClientidentificationOrderByCreatedAtDesc(clientIdentification)
                .stream().map(this::refreshStatusAndSave)
                .filter(w -> w.getStatus() != WarrantyStatus.CERRADA)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtra garantías por placa del vehículo.
      * @param vehiclePlate placa del vehículo
      * @return lista de garantías asociadas al vehículo
      */
    @Transactional
    public List<WarrantyResponseDTO> getWarrantiesByVehicle(String vehiclePlate) {
        return warrantyRepository
                .findByWorkOrderVehicleplateIgnoreCaseOrderByCreatedAtDesc(vehiclePlate)
                .stream().map(this::refreshStatusAndSave)
                .filter(w -> w.getStatus() != WarrantyStatus.CERRADA)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtra garantías por id de servicio cubierto.
     * @param serviceId id del servicio cubierto por la garantía
     * @return lista de garantías asociadas al servicio
     */
    @Transactional
    public List<WarrantyResponseDTO> getWarrantiesByService(Long serviceId) {
        return warrantyRepository.findByServiceId(serviceId)
                .stream().map(this::refreshStatusAndSave)
                .filter(w -> w.getStatus() != WarrantyStatus.CERRADA)
                .map(this::mapToDTO)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Filtra garantías por id de repuesto cubierto.
     * @param sparePartId id del repuesto cubierto por la garantía
     * @return lista de garantías asociadas al repuesto
     */
    @Transactional
    public List<WarrantyResponseDTO> getWarrantiesBySparePart(Long sparePartId) {
        return warrantyRepository.findBySparePartId(sparePartId)
                .stream().map(this::refreshStatusAndSave)
                .filter(w -> w.getStatus() != WarrantyStatus.CERRADA)
                .map(this::mapToDTO)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Búsqueda de texto libre sobre cliente, placa, servicios o repuestos cubiertos.
     * @param text texto de búsqueda libre
     * @return lista de garantías que coinciden con el texto de búsqueda
     */
    @Transactional
    public List<WarrantyResponseDTO> searchWarranties(String text) {
        return warrantyRepository.searchByText(text)
                .stream().map(this::refreshStatusAndSave)
                .filter(w -> w.getStatus() != WarrantyStatus.CERRADA)
                .map(this::mapToDTO)
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .collect(Collectors.toList());
    }

    /**
     * Retorna el detalle de una garantía por su id, actualizando su estado según la fecha actual.
     * @param id id de la garantía
     * @return datos de la garantía con estado actualizado
     */
    @Transactional
    public WarrantyResponseDTO getWarrantyById(Long id) {
        Warranty warranty = findById(id);
        return mapToDTO(refreshStatusAndSave(warranty));
    }

    /**
     * Actualiza los datos de una garantía existente (incluyendo los servicios y
     * repuestos que cubre, que se reemplazan por completo con los enviados).
     * No permite modificar garantías CERRADAS.
     * @param id        id de la garantía
     * @param request   nuevos datos
     * @param updatedBy usuario que realiza la modificación
     * @return datos actualizados de la garantía
     */
    @Transactional
    public WarrantyResponseDTO updateWarranty(Long id, WarrantyRequestDTO request, String updatedBy) {

        Warranty warranty = findById(id);

        if (warranty.getStatus() == WarrantyStatus.CERRADA)
            throw new RuntimeException("No se puede modificar una garantía cerrada.");

        if (request.getEndDate().isBefore(request.getStartDate()))
            throw new RuntimeException("La fecha de fin no puede ser anterior a la fecha de inicio.");

        // Actualizar orden de trabajo si cambió
        if (!warranty.getWorkOrder().getId().equals(request.getWorkOrderId())) {
            WorkOrder workOrder = workOrderRepository.findById(request.getWorkOrderId())
                    .orElseThrow(() -> new RuntimeException(
                            "Orden de trabajo no encontrada con id: " + request.getWorkOrderId()));
            warranty.setWorkOrder(workOrder);
        }

        applyServices(warranty, request.getServiceIds());
        applySpareParts(warranty, request.getSparePartIds());

        warranty.setStartDate(request.getStartDate());
        warranty.setEndDate(request.getEndDate());
        warranty.setObservations(request.getObservations());
        warranty.setUpdatedBy(updatedBy);
        warranty.setStatus(resolveStatus(warranty));

        warrantyRepository.save(warranty);
        log.info("Garantía {} actualizada por {}", id, updatedBy);
        return mapToDTO(warranty);
    }

    /**
     * Cierra una garantía registrando el motivo, fecha y usuario.
     * No permite cerrar garantías ya cerradas.
     * @param id        id de la garantía
     * @param request   motivo de cierre
     * @param closedBy  usuario que realiza el cierre
     * @return datos actualizados de la garantía
     */
    @Transactional
    public WarrantyResponseDTO closeWarranty(Long id, CloseWarrantyRequestDTO request, String closedBy) {

        Warranty warranty = findById(id);

        if (warranty.getStatus() == WarrantyStatus.CERRADA)
            throw new RuntimeException("La garantía ya se encuentra cerrada.");

        warranty.setStatus(WarrantyStatus.CERRADA);
        warranty.setClosureReason(request.getClosureReason());
        warranty.setClosureDate(LocalDateTime.now());
        warranty.setClosedBy(closedBy);
        warranty.setUpdatedBy(closedBy);

        warrantyRepository.save(warranty);
        log.info("Garantía {} cerrada por {}", id, closedBy);
        return mapToDTO(warranty);
    }

    /**
     * Genera un comprobante de garantía en PDF
     * @param id          id de la garantía
     * @param generatedBy usuario que solicita el comprobante
     * @return bytes del PDF generado
     */
    @Transactional
    public byte[] generateWarrantyVoucher(Long id, String generatedBy) {

        Warranty warranty = findById(id);

        // Generar número de comprobante si no tiene uno todavía
        if (warranty.getVoucherNumber() == null) {
            String voucherNum = "GAR-" + String.format("%06d", warranty.getId())
                    + "-" + LocalDate.now().getYear();
            warranty.setVoucherNumber(voucherNum);
        }
        warranty.setVoucherGeneratedAt(LocalDateTime.now());
        warranty.setVoucherGeneratedBy(generatedBy);
        warrantyRepository.save(warranty);

        // Construir PDF con iTextPDF (mismo patrón que PdfGeneratorService existente)
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter  writer = new PdfWriter(baos);
            PdfDocument pdf   = new PdfDocument(writer);
            Document   doc    = new Document(pdf);

            // Encabezado
            doc.add(new Paragraph("COMPROBANTE DE GARANTÍA")
                    .setFontSize(18).setBold());
            doc.add(new Paragraph("N°: " + warranty.getVoucherNumber()));
            doc.add(new Paragraph("Fecha de emisión: "
                    + warranty.getVoucherGeneratedAt().format(
                            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

            doc.add(new LineSeparator(new SolidLine()));

            // Datos del cliente y vehículo
            doc.add(new Paragraph("Cliente        : " + warranty.getWorkOrder().getClientname()));
            doc.add(new Paragraph("Identificación : " + warranty.getWorkOrder().getClientidentification()));
            doc.add(new Paragraph("Teléfono       : " + warranty.getWorkOrder().getClientphone()));
            doc.add(new Paragraph("Vehículo       : "
                    + warranty.getWorkOrder().getVehiclebrand() + " "
                    + warranty.getWorkOrder().getVehiclemodel() + " "
                    + warranty.getWorkOrder().getVehicleyear()));
            doc.add(new Paragraph("Placa          : " + warranty.getWorkOrder().getVehicleplate()));
            doc.add(new Paragraph("N° Orden       : " + warranty.getWorkOrder().getNumberorder()));

            doc.add(new LineSeparator(new SolidLine()));

            // Cobertura: todos los servicios y todos los repuestos cubiertos por ESTA garantía
            if (warranty.getServiceCoverages() != null && !warranty.getServiceCoverages().isEmpty()) {
                String servicios = warranty.getServiceCoverages().stream()
                        .map(c -> c.getService() != null
                                ? c.getService().getName()
                                : c.getNameSnapshot() + " (eliminado del catálogo)")
                        .collect(Collectors.joining(", "));
                doc.add(new Paragraph("Servicios cubiertos : " + servicios));
            }

            if (warranty.getSparePartCoverages() != null && !warranty.getSparePartCoverages().isEmpty()) {
                String repuestos = warranty.getSparePartCoverages().stream()
                        .map(c -> c.getSparePart() != null
                                ? c.getSparePart().getName() + " [Ref: " + c.getSparePart().getReference() + "]"
                                : c.getNameSnapshot() + " [Ref: " + c.getReferenceSnapshot() + "] (eliminado del inventario)")
                        .collect(Collectors.joining(", "));
                doc.add(new Paragraph("Repuestos cubiertos : " + repuestos));
            }

            doc.add(new LineSeparator(new SolidLine()));

            // Vigencia y estado
            doc.add(new Paragraph("Vigencia desde : " + warranty.getStartDate().format(FMT)));
            doc.add(new Paragraph("Vigencia hasta : " + warranty.getEndDate().format(FMT)).setBold());
            doc.add(new Paragraph("Estado         : " + warranty.getStatus()));

            if (warranty.getObservations() != null && !warranty.getObservations().isBlank()) {
                doc.add(new LineSeparator(new SolidLine()));
                doc.add(new Paragraph("Observaciones  : " + warranty.getObservations()));
            }

            doc.add(new LineSeparator(new SolidLine()));

            // Pie
            doc.add(new Paragraph("Emitido por    : " + generatedBy));

            doc.close();

            log.info("Comprobante de garantía {} generado por {}", warranty.getVoucherNumber(), generatedBy);
            return baos.toByteArray();

        } catch (Exception e) {
            log.error("Error generando PDF para garantía {}: {}", id, e.getMessage());
            throw new RuntimeException("Error al generar el comprobante de garantía", e);
        }
    }

    private Warranty findById(Long id) {
        return warrantyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Garantía no encontrada con id: " + id));
    }

    /**
     * Reemplaza la lista de servicios cubiertos por la garantía con los ids recibidos.
     * @param warranty   garantía a modificar
     * @param serviceIds ids de los servicios a asociar (puede ser null o vacío)
     */
    private void applyServices(Warranty warranty, List<Long> serviceIds) {
        warranty.getServiceCoverages().clear();
        if (serviceIds == null) return;
        for (Long serviceId : serviceIds) {
            ServiceEntity service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("Servicio no encontrado con id: " + serviceId));
            WarrantyServiceCoverage coverage = new WarrantyServiceCoverage();
            coverage.setWarranty(warranty);
            coverage.setService(service);
            warranty.getServiceCoverages().add(coverage);
        }
    }

    /**
     * Reemplaza la lista de repuestos cubiertos por la garantía con los ids recibidos.
     * Gracias al cascade ALL + orphanRemoval en Warranty.sparePartCoverages, las filas
     * antiguas que ya no estén en la nueva lista se eliminan automáticamente.
     * @param warranty     garantía a modificar
     * @param sparePartIds ids de los repuestos a asociar (puede ser null o vacío)
     */
    private void applySpareParts(Warranty warranty, List<Long> sparePartIds) {
        warranty.getSparePartCoverages().clear();
        if (sparePartIds == null) return;
        for (Long sparePartId : sparePartIds) {
            SpareParts sparePart = sparePartsRepository.findById(sparePartId)
                    .orElseThrow(() -> new RuntimeException("Repuesto no encontrado con id: " + sparePartId));
            WarrantySparePartCoverage coverage = new WarrantySparePartCoverage();
            coverage.setWarranty(warranty);
            coverage.setSparePart(sparePart);
            warranty.getSparePartCoverages().add(coverage);
        }
    }

    /**
     * Actualiza el estado de la garantía según la fecha actual y la vigencia, guardando cambios si hubo actualización.
     * @param warranty garantía a revisar y actualizar su estado
     * @return garantía con estado actualizado
     */
    private Warranty refreshStatusAndSave(Warranty warranty) {
        if (warranty.getStatus() != WarrantyStatus.CERRADA) {
            WarrantyStatus current = resolveStatus(warranty);
            if (current != warranty.getStatus()) {
                warranty.setStatus(current);
                warrantyRepository.save(warranty);
            }
        }
        return warranty;
    }

    private WarrantyStatus resolveStatus(Warranty warranty) {
        if (warranty.getStatus() == WarrantyStatus.CERRADA)
            return WarrantyStatus.CERRADA;
        return LocalDate.now().isAfter(warranty.getEndDate())
                ? WarrantyStatus.VENCIDA
                : WarrantyStatus.ACTIVA;
    }

    private WarrantyResponseDTO mapToDTO(Warranty w) {
        WarrantyResponseDTO dto = new WarrantyResponseDTO();
        dto.setId(w.getId());
        dto.setVoucherNumber(w.getVoucherNumber());
        // Orden
        dto.setWorkOrderId(w.getWorkOrder().getId());
        dto.setWorkOrderNumber(w.getWorkOrder().getNumberorder());
        dto.setClientName(w.getWorkOrder().getClientname());
        dto.setClientIdentification(w.getWorkOrder().getClientidentification());
        dto.setVehiclePlate(w.getWorkOrder().getVehicleplate());

        List<ServiceCoverageItemDTO> services = w.getServiceCoverages() == null ? List.of()
                : w.getServiceCoverages().stream()
                        .map(c -> {
                            boolean deleted = c.getService() == null;
                            String name = deleted ? c.getNameSnapshot() : c.getService().getName();
                            Long serviceId = deleted ? null : c.getService().getId();
                            return new ServiceCoverageItemDTO(
                                    serviceId,
                                    deleted && name != null ? name + " (eliminado)" : name
                            );
                        })
                        .collect(Collectors.toList());
        dto.setServices(services);

        List<SparePartCoverageItemDTO> spareParts = w.getSparePartCoverages() == null ? List.of()
                : w.getSparePartCoverages().stream()
                        .map(c -> {
                            boolean deleted = c.getSparePart() == null;
                            String name = deleted ? c.getNameSnapshot() : c.getSparePart().getName();
                            String reference = deleted ? c.getReferenceSnapshot() : c.getSparePart().getReference();
                            Long sparePartId = deleted ? null : c.getSparePart().getId();
                            return new SparePartCoverageItemDTO(
                                    sparePartId,
                                    deleted && name != null ? name + " (eliminado)" : name,
                                    reference,
                                    deleted
                            );
                        })
                        .collect(Collectors.toList());
        dto.setSpareParts(spareParts);

        List<String> resumen = new ArrayList<>();
        services.forEach(s -> resumen.add(s.getName()));
        spareParts.forEach(p -> resumen.add(p.getName()));
        dto.setCoverageSummary(resumen.isEmpty() ? "—" : String.join(", ", resumen));

        dto.setStartDate(w.getStartDate());
        dto.setEndDate(w.getEndDate());
        dto.setStatus(w.getStatus());
        dto.setObservations(w.getObservations());
        dto.setClosureReason(w.getClosureReason());
        dto.setClosureDate(w.getClosureDate());
        dto.setClosedBy(w.getClosedBy());
        dto.setVoucherGeneratedAt(w.getVoucherGeneratedAt());
        dto.setVoucherGeneratedBy(w.getVoucherGeneratedBy());
        dto.setCreatedBy(w.getCreatedBy());
        dto.setCreatedAt(w.getCreatedAt());
        dto.setUpdatedBy(w.getUpdatedBy());
        dto.setUpdatedAt(w.getUpdatedAt());
        return dto;
    }
}