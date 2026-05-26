package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.ReviewRequestDTO;
import com.proyecto.TurboMechanics.dto.ReviewResponseDTO;
import com.proyecto.TurboMechanics.entity.Review;
import com.proyecto.TurboMechanics.entity.Users;
import com.proyecto.TurboMechanics.entity.WorkOrder;
import com.proyecto.TurboMechanics.repository.ReviewRepository;
import com.proyecto.TurboMechanics.repository.UsersRepository;
import com.proyecto.TurboMechanics.repository.WorkOrderRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Validated
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository   reviewRepository;
    private final UsersRepository    usersRepository;
    private final WorkOrderRepository workOrderRepository;

    private static final Set<String> OFFENSIVE_WORDS = Set.of(
            "idiota", "imbecil", "estupido", "maldito", "basura", "inutil"
    );
    private static final Pattern SPAM_PATTERN =
            Pattern.compile("(\\b\\w+\\b)(?:\\s+\\1){4,}", Pattern.CASE_INSENSITIVE);

    /**
     * Crea una nueva reseña para una orden de trabajo finalizada. Verifica que el cliente no haya reseñado antes esa orden
     * @param request datos de la reseña a crear (workOrderId, comment, rating)
     * @param userId ID del cliente autenticado (extraído del JWT, para asociar la reseña al usuario)
     * @return datos de la reseña creada
     */
    @Transactional
    public ReviewResponseDTO createReview(@Valid ReviewRequestDTO request, Long userId) {

        Users user = findUser(userId);
        WorkOrder order = findOrder(request.getWorkOrderId());

        // CA-4: solo se puede reseñar una orden ENTREGADO
        if (order.getStateorder() != WorkOrder.StateOrder.ENTREGADO) {
            throw new IllegalStateException(
                    "Solo puedes reseñar un servicio que haya sido entregado.");
        }

        if (reviewRepository.existsByUser_IdAndWorkOrder_IdAndActiveTrue(userId, order.getId())) {
            throw new IllegalStateException(
                    "Ya existe una reseña registrada para esta orden de trabajo.");
        }

        if (reviewRepository.countIdenticalCommentsByUser(userId, request.getComment()) > 0) {
            throw new IllegalStateException(
                    "Tu reseña fue rechazada por duplicidad de contenido.");
        }

        validateContent(request.getComment());

        Review review = new Review();
        review.setUser(user);
        review.setWorkOrder(order);
        review.setComment(request.getComment().trim());
        review.setRating(request.getRating());

        Review saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    /**
     * Elimina lógicamente una reseña por el cliente (solo el autor). Marca la reseña como inactiva para ocultarla.
     * @param reviewId ID de la reseña a eliminar
     * @param userId ID del cliente autenticado (extraído del JWT, para verificar autoría)
     */
    @Transactional
    public void deleteByClient(Long reviewId, Long userId) {
        Review review = reviewRepository.findByIdAndUser_IdAndActiveTrue(reviewId, userId)
                .orElseThrow(() -> new RuntimeException(
                        "Reseña no encontrada o no tienes permisos para eliminarla."));
        review.setActive(false);
        reviewRepository.save(review);
    }

    /**
     * Devuelve todas las reseñas activas, ordenadas por fecha o calificación según el parámetro.
     * @param sortBy opcional: "fecha" (default), "calificacion_asc", "calificacion_desc"
     * @return lista de ReviewResponseDTO con las reseñas activas según el orden solicitado
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> listAll(String sortBy) {
        List<Review> reviews = switch (sortBy == null ? "fecha" : sortBy.toLowerCase()) {
            case "calificacion_asc"  -> reviewRepository.findByActiveTrueOrderByRatingAsc();
            case "calificacion_desc" -> reviewRepository.findByActiveTrueOrderByRatingDesc();
            default                  -> reviewRepository.findByActiveTrueOrderByReviewDateDesc();
        };
        return reviews.stream().map(this::toResponse).toList();
    }

    /**
     * Devuelve las reseñas activas de un cliente específico, ordenadas por fecha descendente.
     * @param userId ID del cliente autenticado
     * @return lista de ReviewResponseDTO del cliente
     */
    @Transactional(readOnly = true)
    public List<ReviewResponseDTO> listByClient(Long userId) {
        return reviewRepository.findByUser_IdAndActiveTrueOrderByReviewDateDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    /**
     * Elimina lógicamente una reseña por moderación (solo admin). Marca la reseña como inactiva para ocultarla.
     * @param reviewId ID de la reseña a moderar
     */
    @Transactional
    public void moderateReview(Long reviewId) {
        Review review = reviewRepository.findByIdAndActiveTrue(reviewId)
                .orElseThrow(() -> new RuntimeException(
                        "Reseña no encontrada o ya fue eliminada."));
        review.setActive(false);
        reviewRepository.save(review);
    }

    private Users findUser(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + userId));
    }

    private WorkOrder findOrder(Long orderId) {
        return workOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Orden de trabajo no encontrada con id: " + orderId));
    }

    //** Valida el contenido de la reseña */
    private void validateContent(String comment) {
        String lower = comment.toLowerCase();
        for (String word : OFFENSIVE_WORDS) {
            if (lower.contains(word)) {
                throw new IllegalArgumentException(
                        "Tu reseña fue rechazada por contener lenguaje inapropiado.");
            }
        }
        if (SPAM_PATTERN.matcher(comment).find()) {
            throw new IllegalArgumentException(
                    "Tu reseña fue rechazada por contener contenido repetitivo (spam).");
        }
    }

    private ReviewResponseDTO toResponse(Review r) {
        return ReviewResponseDTO.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .username(r.getUser().getUsername())
                .workOrderId(r.getWorkOrder().getId())
                .workOrderNumber(r.getWorkOrder().getNumberorder())
                .comment(r.getComment())
                .rating(r.getRating())
                .reviewDate(r.getReviewDate())
                .build();
    }
}