package com.proyecto.TurboMechanics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.TurboMechanics.dto.RegisterMovementRequestDTO;
import com.proyecto.TurboMechanics.entity.Bill;
import com.proyecto.TurboMechanics.entity.MovementPay;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.repository.BillRepository;
import com.proyecto.TurboMechanics.repository.MovementPayRepository;
import com.proyecto.TurboMechanics.repository.UsersRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovementService {

    private final MovementPayRepository movementPayRepository;

    private final BillRepository billRepository;

    private final UsersRepository usersRepository;

    /**
     * registrar el movimiento
     * 
     * @param request dto para el registro del movimiento
     * @return retorna el registro del movimiento
     */
    @Transactional
    public MovementPay register(RegisterMovementRequestDTO request) {

        Bill bill = null;

        if (request.getBillId() != null) {

            bill = billRepository.findById(request.getBillId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Factura no encontrada con id: "
                                    + request.getBillId()));
        }

        Users user = usersRepository.findById(
                request.getRegisterByIdentification())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Usuario no encontrado con id: "
                                + request.getRegisterByIdentification()));

        MovementPay movementPay = MovementPay.builder()
                .type(request.getType())
                .concept(request.getConcept())
                .description(request.getDescription())
                .amount(request.getAmount())
                .bill(bill)
                .registeredBy(user)
                .date(LocalDateTime.now())
                .build();

        return movementPayRepository.save(movementPay);
    }

    /**
     * lista entre fecha
     * 
     * @param start parametro de inicio de fecha
     * @param end   parametro de fin de fecha
     * @return retorna la lista
     */
    public List<MovementPay> listBetween(
            LocalDateTime start,
            LocalDateTime end) {

        return movementPayRepository.findByDateBetween(start, end);
    }

    /**
     * Listar todos los movimientos.
     * 
     * @return retorna la lista de movimientos
     */
    public List<MovementPay> listAll() {
        return movementPayRepository.findAll();
    }
}