package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.WorkEvidenceResponseDTO;
import com.proyecto.TurboMechanics.entity.WorkEvidence;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.repository.WorkEvidenceRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.proyecto.TurboMechanics.enums.EvidenceType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkEvidenceService {

    private final WorkEvidenceRepository evidenceRepository;
    private final WorkOrderRepository    workOrderRepository;

    /** Directorio base donde se guardan los archivos (configurable vía application.yaml) */
    private static final String UPLOAD_DIR = "uploads/evidencias";

    /** Tipos MIME permitidos: imágenes, videos y documentos */
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/avi", "video/quicktime",
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /**
     * Adjunta una evidencia (imagen, video o documento) a una orden de trabajo.
     * @param workOrderId id de la orden de trabajo
     * @param file        archivo adjunto
     * @param description descripción opcional de la evidencia
     * @param uploadedBy  usuario que adjunta la evidencia
     * @return datos de la evidencia guardada
     */
    @Transactional
    public WorkEvidenceResponseDTO uploadEvidence(
            Long workOrderId, MultipartFile file, String description, String uploadedBy) {

        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new RuntimeException(
                        "Orden de trabajo no encontrada con id: " + workOrderId));

        // Validar formato del archivo
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType))
            throw new RuntimeException(
                    "Formato no permitido: " + mimeType
                    + ". Formatos aceptados: imágenes (jpeg, png, gif, webp), "
                    + "videos (mp4, avi, mov) y documentos (pdf, doc, docx).");

        // Determinar tipo de evidencia
        EvidenceType evidenceType = resolveEvidenceType(mimeType);

        // Guardar archivo en disco
        String savedPath = saveFile(file, workOrderId);

        WorkEvidence evidence = new WorkEvidence();
        evidence.setWorkOrder(workOrder);
        evidence.setFileName(file.getOriginalFilename());
        evidence.setEvidenceType(evidenceType);
        evidence.setMimeType(mimeType);
        evidence.setFilePath(savedPath);
        evidence.setFileSizeBytes(file.getSize());
        evidence.setDescription(description);
        evidence.setUploadedBy(uploadedBy);

        evidenceRepository.save(evidence);
        log.info("Evidencia adjuntada a orden {} por {}", workOrderId, uploadedBy);
        return mapToDTO(evidence);
    }

    /**
     * Lista todas las evidencias de una orden de trabajo, con filtro opcional por tipo.
     * @param workOrderId  id de la orden
     * @param evidenceType filtrar por tipo (IMAGEN, VIDEO, DOCUMENTO) — opcional
     * @return lista de evidencias
     */
    @Transactional(readOnly = true)
    public List<WorkEvidenceResponseDTO> getEvidences(
            Long workOrderId, EvidenceType evidenceType) {

        List<WorkEvidence> evidences = evidenceType != null
                ? evidenceRepository.findByWorkOrderIdAndEvidenceTypeOrderByUploadedAtDesc(
                        workOrderId, evidenceType)
                : evidenceRepository.findByWorkOrderIdOrderByUploadedAtDesc(workOrderId);

        return evidences.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Elimina una evidencia por su id, borrando también el archivo físico asociado.
     * @param evidenceId id de la evidencia a eliminar
     */
    @Transactional
    public void deleteEvidence(Long evidenceId) {
        WorkEvidence evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new RuntimeException(
                        "Evidencia no encontrada con id: " + evidenceId));

        // Intentar eliminar el archivo físico
        try {
            Path filePath = Paths.get(evidence.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo físico: {}", evidence.getFilePath());
        }

        evidenceRepository.delete(evidence);
        log.info("Evidencia {} eliminada", evidenceId);
    }

    /**
     * Guarda el archivo adjunto en el sistema de archivos local, organizando por orden de trabajo.
     * @param file archivo a guardar
     * @param workOrderId id de la orden de trabajo para organizar el almacenamiento
     * @return ruta completa donde se guardó el archivo
     */
    private String saveFile(MultipartFile file, Long workOrderId) {
        try {
            Path dir = Paths.get(UPLOAD_DIR, String.valueOf(workOrderId));
            Files.createDirectories(dir);

            String extension = "";
            String originalName = file.getOriginalFilename();
            if (originalName != null && originalName.contains("."))
                extension = originalName.substring(originalName.lastIndexOf('.'));

            String uniqueName = UUID.randomUUID() + extension;
            Path dest = dir.resolve(uniqueName);
            Files.copy(file.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

            return dest.toString();
        } catch (IOException e) {
            log.error("Error guardando archivo: {}", e.getMessage());
            throw new RuntimeException("Error al guardar el archivo adjunto", e);
        }
    }

    private EvidenceType resolveEvidenceType(String mimeType) {
        if (mimeType.startsWith("image/"))  return EvidenceType.IMAGEN;
        if (mimeType.startsWith("video/"))  return EvidenceType.VIDEO;
        return EvidenceType.DOCUMENTO;
    }

    private WorkEvidenceResponseDTO mapToDTO(WorkEvidence e) {
        WorkEvidenceResponseDTO dto = new WorkEvidenceResponseDTO();
        dto.setId(e.getId());
        dto.setWorkOrderId(e.getWorkOrder().getId());
        dto.setWorkOrderNumber(e.getWorkOrder().getNumberorder());
        dto.setFileName(e.getFileName());
        dto.setEvidenceType(e.getEvidenceType());
        dto.setMimeType(e.getMimeType());
        dto.setFilePath(e.getFilePath());
        dto.setFileSizeBytes(e.getFileSizeBytes());
        dto.setDescription(e.getDescription());
        dto.setUploadedBy(e.getUploadedBy());
        dto.setUploadedAt(e.getUploadedAt());
        return dto;
    }
}