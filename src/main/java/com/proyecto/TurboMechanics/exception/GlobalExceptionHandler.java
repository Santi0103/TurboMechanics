package com.proyecto.TurboMechanics.exception;

import com.proyecto.TurboMechanics.dto.MessageResponseDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 * <p>
 * Centraliza el control de errores para TODO el aplicativo (los 28 controllers),
 * garantizando que cualquier excepción que no haya sido capturada manualmente en un
 * controller/service también devuelva un mensaje claro y específico de por qué falló
 * la petición, en el mismo formato que ya consume el frontend: {"message": "..."}.
 * <p>
 * Esto NO reemplaza los try/catch que ya existen en los controllers (esos siguen
 * funcionando igual); este handler solo entra en acción cuando una excepción no fue
 * capturada explícitamente, o cuando ocurre antes de llegar al controller
 * (ej. validación de @Valid, parámetros mal formados, body inválido, etc.).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ───────────────────────────────────────────────────────────
    // 400 - Validación de campos (@Valid en @RequestBody)
    // ───────────────────────────────────────────────────────────

    /**
     * Se lanza cuando un DTO anotado con @Valid no cumple sus validaciones
     * (@NotBlank, @NotNull, @Email, @Size, @Pattern, etc.).
     * Devuelve el detalle de CADA campo que falló, no un mensaje genérico.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponseDTO> handleValidation(MethodArgumentNotValidException e) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(this::formatFieldError)
                .collect(Collectors.joining(" | "));

        if (detalle.isBlank()) {
            detalle = "Los datos enviados no son válidos.";
        }
        return ResponseEntity.badRequest().body(new MessageResponseDTO(detalle));
    }

    private String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
    }

    // ───────────────────────────────────────────────────────────
    // 400 - Validación a nivel de entidad/JPA (no de DTO)
    // ───────────────────────────────────────────────────────────

    /**
     * Se lanza cuando una entidad (anotada con @NotBlank, @NotNull, etc. directamente
     * en la clase @Entity, no en el DTO) falla su validación al hacer flush/save.
     * Este era el caso del ConstraintViolationException que rompía BillService:
     * antes llegaba como 500 sin explicación; ahora dice exactamente qué campo y por qué.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponseDTO> handleConstraintViolation(ConstraintViolationException e) {
        String detalle = e.getConstraintViolations().stream()
                .map(this::formatConstraintViolation)
                .collect(Collectors.joining(" | "));

        if (detalle.isBlank()) {
            detalle = "Los datos no cumplen las reglas de validación.";
        }
        return ResponseEntity.badRequest().body(new MessageResponseDTO(detalle));
    }

    private String formatConstraintViolation(ConstraintViolation<?> v) {
        String campo = v.getPropertyPath().toString();
        return campo + ": " + v.getMessage();
    }

    // ───────────────────────────────────────────────────────────
    // 409 - Violación de restricciones de la base de datos
    // ───────────────────────────────────────────────────────────

    /**
     * Se lanza cuando la base de datos rechaza la operación: clave única duplicada
     * (ej. correo o documento repetido), clave foránea inexistente, columna NOT NULL
     * sin valor, etc. Por defecto esto llega como un 500 con un mensaje técnico de
     * Hibernate/SQL ilegible para el usuario; aquí se traduce a algo entendible.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<MessageResponseDTO> handleDataIntegrity(DataIntegrityViolationException e) {
        String causaRaiz = e.getMostSpecificCause() != null
                ? e.getMostSpecificCause().getMessage()
                : e.getMessage();

        String mensaje;
        if (causaRaiz != null && (causaRaiz.toLowerCase().contains("unique") || causaRaiz.toLowerCase().contains("duplicate"))) {
            mensaje = "Ya existe un registro con ese mismo valor único (correo, documento u otro campo duplicado).";
        } else if (causaRaiz != null && causaRaiz.toLowerCase().contains("foreign key")) {
            mensaje = "La operación hace referencia a un registro relacionado que no existe o no se puede modificar.";
        } else if (causaRaiz != null && (causaRaiz.toLowerCase().contains("null") )) {
            mensaje = "Falta un dato obligatorio que la base de datos requiere.";
        } else {
            mensaje = "No se pudo completar la operación por una restricción de la base de datos.";
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponseDTO(mensaje + " Detalle técnico: " + causaRaiz));
    }

    // ───────────────────────────────────────────────────────────
    // 404 - Entidad no encontrada (JPA)
    // ───────────────────────────────────────────────────────────

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<MessageResponseDTO> handleEntityNotFound(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponseDTO(e.getMessage()));
    }

    // ───────────────────────────────────────────────────────────
    // 400 - Errores de la petición HTTP en sí (antes de llegar al controller)
    // ───────────────────────────────────────────────────────────

    /** El JSON del body está mal formado, vacío, o tiene un tipo de dato incorrecto. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<MessageResponseDTO> handleUnreadableBody(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(
                "El cuerpo de la petición es inválido o tiene un formato incorrecto. Verifica que sea un JSON válido y que los tipos de dato sean correctos."));
    }

    /** Falta un parámetro obligatorio en la URL (@RequestParam sin valor). */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<MessageResponseDTO> handleMissingParam(MissingServletRequestParameterException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(
                "Falta el parámetro obligatorio: " + e.getParameterName()));
    }

    /** Un parámetro de la URL o path variable tiene un tipo incorrecto (ej. texto donde se espera un número). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<MessageResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        String tipoEsperado = e.getRequiredType() != null ? e.getRequiredType().getSimpleName() : "válido";
        return ResponseEntity.badRequest().body(new MessageResponseDTO(
                "El valor '" + e.getValue() + "' enviado en '" + e.getName() + "' no es un " + tipoEsperado + "."));
    }

    /** El endpoint solicitado no existe (ruta mal escrita o sin implementar). */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<MessageResponseDTO> handleNotFound(NoHandlerFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(
                "No existe el recurso solicitado: " + e.getHttpMethod() + " " + e.getRequestURL()));
    }

    /**
     * Spring Boot 4 (Spring Framework 7) ya no usa "throw-exception-if-no-handler-found":
     * por defecto, cuando una ruta no coincide con ningún controller, lanza esta excepción.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<MessageResponseDTO> handleNoResourceFound(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponseDTO(
                "No existe el recurso o ruta solicitada: " + e.getHttpMethod() + " " + e.getResourcePath()));
    }

    /** Se llamó al endpoint con un método HTTP que no soporta (ej. DELETE en un endpoint solo GET). */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<MessageResponseDTO> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(new MessageResponseDTO(
                "El método " + e.getMethod() + " no está permitido en esta ruta. Métodos soportados: " + e.getSupportedHttpMethods()));
    }

    /** El archivo subido supera el tamaño máximo permitido. */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<MessageResponseDTO> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new MessageResponseDTO(
                "El archivo enviado supera el tamaño máximo permitido."));
    }

    // ───────────────────────────────────────────────────────────
    // 400 - Errores de lógica de negocio ya esperados por el código
    // ───────────────────────────────────────────────────────────

    /**
     * RuntimeException e IllegalStateException/IllegalArgumentException genéricas:
     * son las que ya se usan en los services para señalar reglas de negocio
     * ("Ya existe un mecánico con ese documento", etc.). Si algún controller
     * no las capturó manualmente, caen aquí con su mensaje real intacto.
     */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<MessageResponseDTO> handleIllegal(RuntimeException e) {
        return ResponseEntity.badRequest().body(new MessageResponseDTO(e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<MessageResponseDTO> handleRuntime(RuntimeException e) {
        String mensaje = e.getMessage() != null
                ? e.getMessage()
                : "Ocurrió un error inesperado de tipo " + e.getClass().getSimpleName();
        return ResponseEntity.badRequest().body(new MessageResponseDTO(mensaje));
    }

    // ───────────────────────────────────────────────────────────
    // 500 - Cualquier otra cosa no prevista (último recurso)
    // ───────────────────────────────────────────────────────────

    /**
     * Red de seguridad final: cualquier excepción que no sea RuntimeException
     * (ej. errores de IO, de configuración, NPE que escapó como Error, etc.).
     * Nunca se devuelve el stacktrace genérico de Spring: siempre se informa
     * la clase y el mensaje real de la excepción para poder diagnosticarla.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponseDTO> handleGeneric(Exception e) {
        e.printStackTrace();
        String mensaje = "Error interno (" + e.getClass().getSimpleName() + "): "
                + (e.getMessage() != null ? e.getMessage() : "sin detalle adicional, revisa los logs del servidor.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponseDTO(mensaje));
    }
}