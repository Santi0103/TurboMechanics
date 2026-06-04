package com.proyecto.TurboMechanics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentResponseDTO {

    /** Id interno del Payment creado en nuestra BD */
    private Long paymentId;

    /** Referencia externa enviada a MercadoPago */
    private String externalReference;

    /**
     * URL de Checkout Pro donde el cliente completa el pago.
     * El frontend debe redirigir aquí al usuario.
     * Null si el pago fue aprobado directamente (tarjeta tokenizada).
     */
    private String initPoint;

    /** Id de preferencia de MercadoPago */
    private String preferenceId;

    /** Estado inicial: siempre PENDING */
    private String status;

    /** Llave pública para el frontend (Brick de MercadoPago) */
    private String publicKey;
}