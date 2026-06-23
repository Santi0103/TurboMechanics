package com.proyecto.TurboMechanics.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyecto.TurboMechanics.dto.SpareSaleResponseDTO;
import com.proyecto.TurboMechanics.entity.SpareSale;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.repository.SpareSaleRepository;
import com.proyecto.TurboMechanics.security.RequiresRole;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/spare-sales")
@RequiredArgsConstructor
public class SpareSaleController {

    private final SpareSaleRepository spareSaleRepository;

    /**
     * List all spare part sales — admin only
     */
    @GetMapping
    @RequiresRole({ RolEnum.ADMIN })
    public ResponseEntity<List<SpareSaleResponseDTO>> listAll() {
        try {
            List<SpareSaleResponseDTO> list = spareSaleRepository
                .findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private SpareSaleResponseDTO toDTO(SpareSale s) {
        boolean sparePartDeleted = s.getSparePart() == null;
        String name = sparePartDeleted ? s.getSparePartNameSnapshot() : s.getSparePart().getName();
        String reference = sparePartDeleted ? s.getSparePartReferenceSnapshot() : s.getSparePart().getReference();
        String category = sparePartDeleted ? s.getSparePartCategorySnapshot() : s.getSparePart().getCategory();

        return SpareSaleResponseDTO.builder()
            .id(s.getId())
            .sparePartName(sparePartDeleted ? name + " (eliminado)" : name)
            .sparePartReference(reference)
            .sparePartCategory(category)
            .sparePartDeleted(sparePartDeleted)
            .payerEmail(s.getPayerEmail())
            .price(s.getPrice())
            .externalReference(s.getExternalReference())
            .preferenceId(s.getPreferenceId())
            .createdAt(s.getCreatedAt())
            .status(s.getStatus().name())
            .build();
    }
}