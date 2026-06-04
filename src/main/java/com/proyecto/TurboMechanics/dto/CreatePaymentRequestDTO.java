package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreatePaymentRequestDTO {

    /** Id de la factura a pagar */
    @NotNull(message = "El id de la factura es obligatorio")
    @Positive
    private Long billId;

    /**
     * Método de pago elegido por el cliente.
     * Valores válidos: credit_card | debit_card | pse | efecty | bank_transfer
     */
    private String paymentMethod;

    /** Email del pagador — requerido por MercadoPago */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String payerEmail;

    /** Nombre del pagador */
    private String payerFirstName;

    /** Apellido del pagador */
    private String payerLastName;

    /**
     * Número de identificación del pagador.
     * Requerido para PSE.
     */
    private String payerIdentificationNumber;

    /**
     * Tipo de identificación: CC | NIT | CE
     * Requerido para PSE.
     */
    private String payerIdentificationType;
}