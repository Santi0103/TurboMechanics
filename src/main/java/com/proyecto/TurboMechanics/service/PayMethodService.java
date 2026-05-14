package com.proyecto.TurboMechanics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.proyecto.TurboMechanics.entity.PayMethod;
import com.proyecto.TurboMechanics.repository.PayMethodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PayMethodService {
    
    private final PayMethodRepository payMethodRepository;
 
    /**
     * lista todos los metodos de pagos activos
     * @return retorna la lista de los metodos de pagos activos
     */
    public List<PayMethod> listActive() {
        return payMethodRepository.findByActiveTrue();
    }
    
    /**
     * 
     *Lista todos los metodos de pago
     * @return Retorna la lista de los metodos de pago
     */
    public List<PayMethod> listAll() {
        return payMethodRepository.findAll();
    }

    /**
     * crear un nuevo metodo de pago
     * @param method activa el metodo de pago
     * @return retorna el metodo de pago creado
     */
    public PayMethod create(PayMethod method) {
        method.setActive(true);
        return payMethodRepository.save(method);
    }
 
    /**
     * Actualiza un método de pago
     * 
     * @param id   id del método de pago
     * @param data información a actualizar
     * @return método de pago actualizado
     */
    public PayMethod update(Long id, PayMethod data) {

        PayMethod existing = payMethodRepository.findById(id)
            .orElseThrow(() ->
                new RuntimeException(
                    "Método de pago no encontrado con id: " + id));

        existing.setName(data.getName());
        existing.setDescription(data.getDescription());
        existing.setConfigJson(data.getConfigJson());
        existing.setActive(data.isActive());

        return payMethodRepository.save(existing);
    }
}
