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

    /** Palabras ofensivas/insultos personales en español (incluye coloquialismos colombianos).
     *  No incluye críticas legítimas al servicio (ej. "malo", "lento", "tramposo", "mentiroso"),
     *  solo lenguaje irrespetuoso, despectivo o vulgar dirigido a personas. */
    private static final Set<String> OFFENSIVE_WORDS = Set.of(
            // Groserías / vulgaridades
            "idiota", "idiotas", "imbecil", "imbeciles", "imbécil",
            "estupido", "estupida", "estupidos", "estupidas", "estúpido", "estúpida",
            "maldito", "maldita", "malditos", "malditas",
            "mierda", "carajo", "joder",
            "puta", "puto", "putas", "putos", "perra", "zorra",
            "cabron", "cabrón", "verga", "pija", "malparido", "malparida", "gonorrea",
            "marica", "maricon", "maricón", "gilipollas",

            // Insultos de torpeza / poca inteligencia (coloquial)
            "guevon", "güevón", "huevon", "huevón", "aguevado", "agüevado", "aguevao", "agüevao",
            "pendejo", "pendeja", "pendejos", "pendejas", "pendejete",
            "bobo", "boba", "bobazo", "bobolon", "bobolón",
            "menso", "mensa", "tonto", "tonta", "zoquete",
            "atembado", "atembada", "atarantado", "atarantada",
            "gil", "lerdo", "lerda", "pasmado", "pasmada",
            "bruto", "bruta", "brutico", "brutica", "burro", "burra",
            "bestia", "tronco de burro",
            "tarado", "tarada", "cretino", "cretina", "pelmazo",
            "inutil", "inutiles", "inútil",

            // Terquedad / necedad
            "cabezon", "cabezón", "cabezadura", "terco", "terca", "necio", "necia",

            // Ridiculez / vergüenza
            "ridiculo", "ridicula", "ridículo", "ridícula", "payaso", "payasa", "fantoche", "farolero", "farolera",

            // Suciedad / mal gusto / tacañería despectiva
            "caspa", "chanda", "chandoso", "chandosa", "chichipato", "chichipata",
            "miserable", "corroncho", "corroncha", "guiso", "ordinario", "ordinaria", "patan", "patán", "grosero", "grosera",

            // Adulación / chisme / entrometido
            "lambon", "lambón", "lagarto", "lagarta", "sapo",
            "chismoso", "chismosa", "boquisuelto", "boquisuelta",
            "metido", "metida", "entrometido", "entrometida",
            "descarado", "descarada", "caradura", "sinverguenza", "sinvergüenza",

            // Pereza / aprovechamiento
            "haragan", "haragán", "haragana", "zangano", "zángano", "zangana",
            "aprovechado", "aprovechada", "vividor", "vividora",

            // Engaño / falsedad despectiva
            "embaucador", "embaucadora", "embustero", "embustera",
            "charlatan", "charlatán", "charlatana", "farsante",
            "hipocrita", "hipócrita", "fariseo", "fariseica",
            "culebrero", "culebrera", "cuentero", "cuentera", "vendehumo", "chicanero", "chicanera", "recochero", "recochera",

            // Pesadez / mal genio
            "jarto", "jarta", "canson", "cansón", "cansona", "mamon", "mamón", "mamona",
            "intensito", "intensita", "fastidioso", "fastidiosa",
            "amargado", "amargada", "malgeniado", "malgeniada", "cascarrabias",

            // Envidia / arrogancia
            "envidioso", "envidiosa", "celoso", "celosa",
            "creido", "creída", "agrandado", "agrandada", "presumido", "presumida", "engreido", "engreído", "engreída",

            // Escándalo / mala conducta
            "desjuiciado", "desjuiciada", "alborotado", "alborotada", "escandaloso", "escandalosa",
            "achantado", "achantada", "arrastrado", "arrastrada", "calidoso", "calidosa", "aletoso", "aletosa",

            // Delincuencia despectiva
            "bandido", "bandida", "pillo", "pilla", "malicioso", "maliciosa"
    );

    /**
     * Patrón único precompilado que combina todas las OFFENSIVE_WORDS con límites de palabra (\b),
     * para no recompilar un regex por cada palabra en cada validación (mejor rendimiento).
     */
    private static final Pattern OFFENSIVE_PATTERN = Pattern.compile(
            "\\b(" + OFFENSIVE_WORDS.stream()
                    .map(Pattern::quote)
                    .collect(java.util.stream.Collectors.joining("|")) + ")\\b",
            Pattern.CASE_INSENSITIVE
    );

    /** Detecta una misma palabra repetida 5 o más veces consecutivas (spam tipo "bueno bueno bueno..."). */
    private static final Pattern SPAM_PATTERN =
            Pattern.compile("(\\b\\w+\\b)(?:\\s+\\1){4,}", Pattern.CASE_INSENSITIVE);

    /** Detecta un mismo carácter repetido 5 o más veces seguidas (ej. "aaaaaa", "????", "111111"). */
    private static final Pattern REPEATED_CHAR_PATTERN =
            Pattern.compile("(.)\\1{4,}");

    /** Exige que el comentario tenga al menos una letra (rechaza reseñas de solo números, espacios o símbolos). */
    private static final Pattern HAS_LETTER_PATTERN =
            Pattern.compile(".*[A-Za-zÁÉÍÓÚÑáéíóúñ].*");

    /** Cantidad mínima de palabras distintas que debe tener un comentario para considerarse contenido real. */
    private static final int MIN_DISTINCT_WORDS = 2;

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

        // RF 10.5: la orden de trabajo debe pertenecer al cliente autenticado.
        // Solo se puede reseñar una orden propia, identificada por el documento del cliente
        // registrado en la orden vs. la identificación del usuario que inició sesión.
        if (user.getIdentification() == null
                || order.getClientidentification() == null
                || !order.getClientidentification().trim().equals(String.valueOf(user.getIdentification()))) {
            throw new IllegalStateException(
                    "Solo puedes reseñar órdenes de trabajo registradas con tu propia identificación.");
        }

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

    //** Valida el contenido de la reseña (RF 10.5) */
    private void validateContent(String comment) {
        String trimmed = comment.trim();

        if (!HAS_LETTER_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    "Tu reseña debe contener texto, no solo números o símbolos.");
        }

        String lower = trimmed.toLowerCase();

        if (OFFENSIVE_PATTERN.matcher(lower).find()) {
            throw new IllegalArgumentException(
                    "Tu reseña fue rechazada por contener lenguaje inapropiado.");
        }

        if (SPAM_PATTERN.matcher(trimmed).find()) {
            throw new IllegalArgumentException(
                    "Tu reseña fue rechazada por contener contenido repetitivo (spam).");
        }

        if (REPEATED_CHAR_PATTERN.matcher(trimmed).find()) {
            throw new IllegalArgumentException(
                    "Tu reseña fue rechazada por contener caracteres repetidos sin sentido.");
        }

        long distinctWords = java.util.Arrays.stream(trimmed.split("\\s+"))
                .map(String::toLowerCase)
                .distinct()
                .count();
        if (distinctWords < MIN_DISTINCT_WORDS) {
            throw new IllegalArgumentException(
                    "Tu reseña es demasiado corta o poco descriptiva. Cuéntanos un poco más sobre tu experiencia.");
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