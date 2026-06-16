package com.proyecto.TurboMechanics.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TiendaCompraRequestDTO {

    /** Id del repuesto que se quiere comprar */
    @NotNull(message = "El id del repuesto es obligatorio")
    @Positive
    private Long sparePartId;

    /** Email del comprador — requerido por MercadoPago */
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Email inválido")
    private String payerEmail;

    /** Nombre del comprador */
    private String payerFirstName;

    /** Apellido del comprador */
    private String payerLastName;
}
