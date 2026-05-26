package com.proyecto.TurboMechanics.controllers;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import com.proyecto.TurboMechanics.dto.ReviewRequestDTO;
import com.proyecto.TurboMechanics.dto.ReviewResponseDTO;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.security.RequiresRole;
import com.proyecto.TurboMechanics.service.ReviewService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Crea una nueva reseña para una orden de trabajo finalizada. Verifica que el cliente no haya reseñado antes esa orden
     * @param request datos de la reseña a crear (workOrderId, comment, rating)
     * @param httpRequest objeto de la solicitud HTTP para extraer el userId del JWT y asociar la reseña al cliente autenticado
     * @return datos de la reseña creada o error si no se cumplen las validaciones (orden no entregada,
     *  reseña duplicada, lenguaje ofensivo, etc.)
     */
    @PostMapping
    @RequiresRole({RolEnum.CLIENTE})
    public ResponseEntity<?> createReview(
            @Valid @RequestBody ReviewRequestDTO request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            ReviewResponseDTO response = reviewService.createReview(request, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new MessageResponseDTO(e.getMessage()));
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error inesperado al registrar la reseña."));
        }
    }

    /**
     * Permite al cliente eliminar una reseña que él mismo escribió (eliminación lógica). 
     * El ID del cliente se extrae del JWT para verificar que solo pueda eliminar sus propias reseñas.
     * @param id ID de la reseña a eliminar
     * @param httpRequest objeto de la solicitud HTTP para extraer el userId del JWT
     * @return 200 OK con mensaje de confirmación, o 404 si no se encuentra la reseña o no pertenece al cliente autenticado
     */
    @DeleteMapping("/{id}")
    @RequiresRole({RolEnum.CLIENTE})
    public ResponseEntity<?> deleteReview(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            reviewService.deleteByClient(id, userId);
            return ResponseEntity.ok(new MessageResponseDTO("Reseña eliminada exitosamente."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    /**
     * Devuelve todas las reseñas activas, ordenadas por fecha o calificación según el parámetro.
     * @param ordenar opcional: "fecha" (default), "calificacion_asc", "calificacion_desc"
     * @return 200 OK con la lista de reseñas, o 500 si hay error inesperado al consultar
     */
    @GetMapping
    @RequiresRole({RolEnum.CLIENTE, RolEnum.ADMIN})
    public ResponseEntity<?> listReviews(
            @RequestParam(required = false, defaultValue = "fecha") String ordenar) {
        try {
            List<ReviewResponseDTO> response = reviewService.listAll(ordenar);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error al consultar las reseñas."));
        }
    }

    /**
     * Devuelve las reseñas activas del cliente autenticado, ordenadas por fecha descendente.
     * @param httpRequest objeto de la solicitud HTTP para extraer el userId del JWT
     * @return 200 OK con la lista de reseñas del cliente, o 500 si hay error inesperado
     */
    @GetMapping("/my")
    @RequiresRole({RolEnum.CLIENTE})
    public ResponseEntity<?> myReviews(HttpServletRequest httpRequest) {
        try {
            Long userId = extractUserId(httpRequest);
            List<ReviewResponseDTO> response = reviewService.listByClient(userId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new MessageResponseDTO("Error al consultar tus reseñas."));
        }
    }

    /**
     * Permite al administrador eliminar una reseña por moderación (eliminación lógica).
     * @param id ID de la reseña a moderar
     * @return 200 OK con mensaje de confirmación
     */
    @DeleteMapping("/{id}/moderate")
    @RequiresRole({RolEnum.ADMIN})
    public ResponseEntity<?> moderateReview(@PathVariable Long id) {
        try {
            reviewService.moderateReview(id);
            return ResponseEntity.ok(new MessageResponseDTO("Reseña eliminada exitosamente por el administrador."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponseDTO(e.getMessage()));
        }
    }

    private Long extractUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");
        if (userId == null) throw new RuntimeException("No se pudo obtener el usuario autenticado.");
        return Long.parseLong(userId.toString());
    }
}