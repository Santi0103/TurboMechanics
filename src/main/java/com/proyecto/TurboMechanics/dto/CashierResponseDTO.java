package com.proyecto.TurboMechanics.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor  
public class CashierResponseDTO {

    /** Fecha de inicio del cierre de caja */
    private LocalDate start;

    /** Fecha final del cierre de caja */
    private LocalDate end;

    /** Total de ingresos registrados */
    private BigDecimal inputs;

    /** Total de egresos registrados */
    private BigDecimal outputs;

    /** Balance final de caja */
    private BigDecimal balance;
}