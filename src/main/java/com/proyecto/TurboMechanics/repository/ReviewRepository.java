package com.proyecto.TurboMechanics.repository;

import com.proyecto.TurboMechanics.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /** Verifica si ya existe una reseña activa del usuario para esa orden */
    boolean existsByUser_IdAndWorkOrder_IdAndActiveTrue(Long userId, Long workOrderId);

    /** Todas las reseñas activas, ordenadas por fecha descendente */
    List<Review> findByActiveTrueOrderByReviewDateDesc();

    /** Reseñas activas de un usuario específico, ordenadas por fecha descendente */
    List<Review> findByUser_IdAndActiveTrueOrderByReviewDateDesc(Long userId);

    /** Busca una reseña activa por id y usuario (para que el cliente solo borre las suyas) */
    Optional<Review> findByIdAndUser_IdAndActiveTrue(Long reviewId, Long userId);

    /** Busca cualquier reseña activa por id (para el administrador) */
    Optional<Review> findByIdAndActiveTrue(Long reviewId);

    /** Reseñas activas ordenadas por calificación ascendente */
    List<Review> findByActiveTrueOrderByRatingAsc();

    /** Reseñas activas ordenadas por calificación descendente */
    List<Review> findByActiveTrueOrderByRatingDesc();

    /** Detecta posible spam: reseñas activas con comentario idéntico del mismo usuario */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId AND LOWER(r.comment) = LOWER(:comment) AND r.active = true")
    long countIdenticalCommentsByUser(@Param("userId") Long userId, @Param("comment") String comment);
}