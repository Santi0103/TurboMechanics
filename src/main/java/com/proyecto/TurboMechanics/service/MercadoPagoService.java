package com.proyecto.TurboMechanics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferencePayerRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;

import com.proyecto.TurboMechanics.dto.CreatePaymentRequestDTO;
import com.proyecto.TurboMechanics.dto.CreatePaymentResponseDTO;
import com.proyecto.TurboMechanics.dto.MercadoPagoWebhookDTO;
import com.proyecto.TurboMechanics.entity.Bill;
import com.proyecto.TurboMechanics.enums.PaymentStatus;
import com.proyecto.TurboMechanics.enums.SpareSaleStatus;
import com.proyecto.TurboMechanics.enums.StatusBill;
import com.proyecto.TurboMechanics.repository.BillRepository;
import com.proyecto.TurboMechanics.repository.PaymentRepository;
import com.proyecto.TurboMechanics.repository.SpareSaleRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MercadoPagoService {

    private final BillRepository      billRepository;
    private final PaymentRepository   paymentRepository;
    private final SpareSaleRepository spareSaleRepository;

    @Value("${mercadopago.public-key}")
    private String publicKey;

    @Value("${mercadopago.notification-url}")
    private String notificationUrl;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Crear la referencia de pago para facturas del taller
     * @param request dto para crear la referencia
     * @return retorna la referencia creada
     */
    @Transactional
    public CreatePaymentResponseDTO createPreference(CreatePaymentRequestDTO request) {

        Bill bill = billRepository.findById(request.getBillId())
            .orElseThrow(() -> new EntityNotFoundException(
                "Factura no encontrada: " + request.getBillId()));

        if (bill.getStatus() == StatusBill.Paid) {
            throw new IllegalStateException(
                "La factura " + bill.getNumBill() + " ya está pagada.");
        }

        String externalReference = bill.getNumBill() + "-" + System.currentTimeMillis();

        try {
            PreferenceClient client = new PreferenceClient();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(bill.getNumBill())
                .title("Servicio de taller – Factura " + bill.getNumBill())
                .description(bill.getWorkOrder() != null
                    ? "Orden de trabajo: " + bill.getWorkOrder().getNumberorder()
                    : "Servicio de mantenimiento")
                .quantity(1)
                .unitPrice(bill.getTotal())
                .currencyId("COP")
                .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(frontendUrl + "/pagos/resultado?status=success&ref=" + externalReference)
                .failure(frontendUrl + "/pagos/resultado?status=failure&ref=" + externalReference)
                .pending(frontendUrl + "/pagos/resultado?status=pending&ref=" + externalReference)
                .build();

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(request.getPayerEmail())
                .name(request.getPayerFirstName())
                .surname(request.getPayerLastName())
                .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(List.of(item))
                .payer(payer)
                .backUrls(backUrls)
                .externalReference(externalReference)
                .notificationUrl(notificationUrl)
                .statementDescriptor("TurboMechanics")
                .build();

            Preference preference = client.create(preferenceRequest);

            com.proyecto.TurboMechanics.entity.Payment payment =
                com.proyecto.TurboMechanics.entity.Payment.builder()
                    .bill(bill)
                    .externalReference(externalReference)
                    .amount(bill.getTotal())
                    .status(PaymentStatus.PENDING)
                    .initPoint(preference.getInitPoint())
                    .mpPreferenceId(preference.getId())
                    .createdAt(LocalDateTime.now())
                    .build();

            com.proyecto.TurboMechanics.entity.Payment saved =
                paymentRepository.save(payment);

            log.info("Preferencia MP creada: id={} ref={}", preference.getId(), externalReference);

            return new CreatePaymentResponseDTO(
                saved.getId(),
                externalReference,
                preference.getInitPoint(),
                preference.getId(),
                "PENDING",
                publicKey
            );

        } catch (MPApiException e) {
            log.error("Error API MercadoPago: {} - {}", e.getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("Error al crear el pago en MercadoPago: " + e.getMessage(), e);
        } catch (MPException e) {
            log.error("Error SDK MercadoPago: {}", e.getMessage());
            throw new RuntimeException("Error al conectar con MercadoPago", e);
        }
    }

    /**
     * Procesar webhook de MercadoPago — actualiza facturas y ventas de tienda
     * @param webhook dto que procesa el webhook
     */
    @Transactional
    public void processWebhook(MercadoPagoWebhookDTO webhook) {

        if (!"payment".equalsIgnoreCase(webhook.getType())) {
            log.info("Webhook MP ignorado: type={}", webhook.getType());
            return;
        }

        String paymentIdStr = webhook.getData().getId();
        if (paymentIdStr == null) {
            log.warn("Webhook MP sin id de pago.");
            return;
        }

        try {
            Long mpPaymentId = Long.parseLong(paymentIdStr);

            PaymentClient client = new PaymentClient();
            Payment mpPayment    = client.get(mpPaymentId);

            String externalRef = mpPayment.getExternalReference();
            String mpStatus    = mpPayment.getStatus();
            String mpMethod    = mpPayment.getPaymentMethodId();

            log.info("Webhook MP: id={} status={} ref={}", mpPaymentId, mpStatus, externalRef);

            PaymentStatus newStatus = mapStatus(mpStatus);

            // ── Venta de tienda (referencia empieza con REP-) ──────────────
            if (externalRef != null && externalRef.startsWith("REP-")) {
                spareSaleRepository.findByExternalReference(externalRef).ifPresent(sale -> {
                    SpareSaleStatus saleStatus = switch (newStatus) {
                        case APPROVED  -> SpareSaleStatus.APPROVED;
                        case REJECTED  -> SpareSaleStatus.REJECTED;
                        case CANCELLED -> SpareSaleStatus.CANCELLED;
                        default        -> SpareSaleStatus.PENDING;
                    };
                    sale.setStatus(saleStatus);
                    spareSaleRepository.save(sale);
                    log.info("SpareSale {} actualizada a {}", sale.getId(), saleStatus);
                });
                return;
            }

            // ── Factura del taller ─────────────────────────────────────────
            com.proyecto.TurboMechanics.entity.Payment payment =
                paymentRepository.findByExternalReference(externalRef)
                    .orElseGet(() -> paymentRepository.findByMpPaymentId(mpPaymentId)
                        .orElseThrow(() -> new EntityNotFoundException(
                            "Pago no encontrado para ref: " + externalRef)));

            payment.setStatus(newStatus);
            payment.setMpPaymentId(mpPaymentId);
            payment.setPaymentMethod(mpMethod);
            paymentRepository.save(payment);

            if (newStatus == PaymentStatus.APPROVED) {
                Bill bill = payment.getBill();
                bill.setStatus(StatusBill.Paid);
                billRepository.save(bill);
                log.info("Factura {} marcada como PAGADA.", bill.getNumBill());
            }

        } catch (NumberFormatException e) {
            log.error("Id de pago inválido: {}", paymentIdStr);
        } catch (MPApiException e) {
            log.error("Error consultando pago MP {}: {}", paymentIdStr, e.getMessage());
            throw new RuntimeException("Error al consultar el pago en MercadoPago", e);
        } catch (MPException e) {
            log.error("Error SDK al consultar pago MP: {}", e.getMessage());
            throw new RuntimeException("Error al conectar con MercadoPago", e);
        }
    }

    /**
     * Consultar el estado de un pago en mercado pago
     * @param mpPaymentId id del pago
     * @return retorna la consulta del estado
     */
    public PaymentStatus checkPaymentStatus(Long mpPaymentId) {
        try {
            PaymentClient client = new PaymentClient();
            Payment mp = client.get(mpPaymentId);
            return mapStatus(mp.getStatus());
        } catch (MPException | MPApiException e) {
            log.error("Error consultando estado MP: {}", e.getMessage());
            throw new RuntimeException("No se pudo consultar el estado del pago", e);
        }
    }

    /**
     * Historial de pagos de una factura
     * @param billId id de la factura
     * @return retorna el historial de las facturas
     */
    public List<com.proyecto.TurboMechanics.entity.Payment> getPaymentsByBill(Long billId) {
        return paymentRepository.findByBillId(billId);
    }

    /**
     * Crear preferencia de pago para compra de repuesto en tienda.
     * No requiere Bill — crea la preferencia directamente con el precio del repuesto.
     */
    @Transactional
    public CreatePaymentResponseDTO createPreferenceForRepuesto(
            String nombre,
            String referencia,
            java.math.BigDecimal precio,
            String payerEmail,
            String payerFirstName,
            String payerLastName) {

        String externalReference = "REP-" + referencia + "-" + System.currentTimeMillis();

        try {
            PreferenceClient client = new PreferenceClient();

            PreferenceItemRequest item = PreferenceItemRequest.builder()
                .id(referencia)
                .title("Repuesto: " + nombre)
                .description("Compra de repuesto ref: " + referencia)
                .quantity(1)
                .unitPrice(precio)
                .currencyId("COP")
                .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(frontendUrl + "/pagos/resultado?status=success&ref=" + externalReference)
                .failure(frontendUrl + "/pagos/resultado?status=failure&ref=" + externalReference)
                .pending(frontendUrl + "/pagos/resultado?status=pending&ref=" + externalReference)
                .build();

            PreferencePayerRequest payer = PreferencePayerRequest.builder()
                .email(payerEmail)
                .name(payerFirstName)
                .surname(payerLastName)
                .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(List.of(item))
                .payer(payer)
                .backUrls(backUrls)
                .externalReference(externalReference)
                .notificationUrl(notificationUrl)
                .statementDescriptor("TurboMechanics")
                .build();

            Preference preference = client.create(preferenceRequest);

            log.info("Preferencia MP tienda creada: id={} ref={}", preference.getId(), externalReference);

            return new CreatePaymentResponseDTO(
                null,
                externalReference,
                preference.getInitPoint(),
                preference.getId(),
                "PENDING",
                publicKey
            );

        } catch (MPApiException e) {
            log.error("Error API MercadoPago tienda: {} - {}", e.getStatusCode(), e.getApiResponse().getContent());
            throw new RuntimeException("Error al crear el pago en MercadoPago: " + e.getMessage(), e);
        } catch (MPException e) {
            log.error("Error SDK MercadoPago tienda: {}", e.getMessage());
            throw new RuntimeException("Error al conectar con MercadoPago", e);
        }
    }

    /**
     * Mapeo del estado de mercado pago
     * @param mpStatus estado de mercado pago
     * @return retorna el mapeo de mercado pago
     */
    private PaymentStatus mapStatus(String mpStatus) {
        if (mpStatus == null) return PaymentStatus.PENDING;
        return switch (mpStatus.toLowerCase()) {
            case "approved"                -> PaymentStatus.APPROVED;
            case "in_process", "pending",
                 "authorized"             -> PaymentStatus.IN_PROCESS;
            case "rejected"               -> PaymentStatus.REJECTED;
            case "cancelled"              -> PaymentStatus.CANCELLED;
            case "refunded", "charged_back" -> PaymentStatus.REFUNDED;
            default                       -> PaymentStatus.ERROR;
        };
    }
}