package com.proyecto.TurboMechanics.service;

import com.proyecto.TurboMechanics.dto.ChatbotRequestDTO;
import com.proyecto.TurboMechanics.dto.ChatbotResponseDTO;
import com.proyecto.TurboMechanics.entity.*;
import com.proyecto.TurboMechanics.enums.RolEnum;
import com.proyecto.TurboMechanics.repository.*;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    private final RestTemplate restTemplate;

    private final ServiceRepository serviceRepository;
    private final WorkshopRepository workshopRepository;
    private final AppointmentRepository appointmentRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WarrantyRepository warrantyRepository;
    private final SparePartsRepository sparePartsRepository;
    private final MechanicRepository mechanicRepository;
    private final UsersRepository usersRepository;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Set<String> BLOCKED_WORDS = Set.of(
        "puta","putas","puto","putos","mierda","mierdas","coño","coños", 
        "culo","culos","joder","hostia","verga","hijueputa","hijueputas","gonorrea","gonorreas",
         "malparido","malparidos","mlp","mlps","hp","hptas", "idiota","idiotas","imbeciles",
         "imbecil","estupido","estupidos","pendejo","pendejos","marica","maricas","mks","mk",
         "maricon","hdp","culero","pinche","cabrón","cabron","chingada","chingar","carechimba","carechimbas",
         "caremonda","careverga","careculo", "chimba","chimbas","chimbo","chimbos","guevon","guevón","guevones",
         "huevon","huevón","huevones","webon","webón","wevon","wevón","lampara","lámpara","zorra","zorras","perra","perras",
        "bastardo","bastardos","mamon","mamón","mamones","pelotudo","pelotudos","tarado","tarados","cretino","cretinos",
        "gilipollas","gilipollas","capullo","capullos","mamaguevo","mamaguevo","mamaguevos","mamagüevo","come mierda",
        "comemierda","carepicha","carepichas","picha","pichas","monda","mondas","cagada","cagadas","cagon","cagón","cagones",
        "cabrona","cabronas","chingado","chingados","chingona","chingon","chingones","culiao","culiada","culiados",
        "conchesumadre","conchetumadre","ctm","csm","boludo","boludos","forro","forros","mongolico","mongólico","mongolicos",
        "mongólicos","baboso","babosos","payaso","payasos","burro","burros","mamerto","mamertos","atembado","atembados",
        "estupida","estúpida","estupidas","estúpidas","idiota","idiotas","subnormal","subnormales"
    );

    public ChatbotResponseDTO chat(ChatbotRequestDTO request, Long rolId, Long userId) {

        String userMessage = request.getMessage().trim();

        if (containsBlockedWord(userMessage)) {
            return new ChatbotResponseDTO(
                "Por favor usa un lenguaje respetuoso. Estoy aquí para ayudarte con consultas relacionadas al taller.",
                resolveRoleName(rolId)
            );
        }

        RolEnum rol = getRolEnum(rolId);
        String systemPrompt = buildSystemPrompt(rol) + buildContext(rol, userId);

        // Gemini recibe el system prompt como primer turno del modelo
        Map<String, Object> systemPart = Map.of("text", systemPrompt);
        Map<String, Object> systemContent = Map.of(
            "role", "model",
            "parts", List.of(systemPart)
        );

        Map<String, Object> userPart = Map.of("text", userMessage);
        Map<String, Object> userContent = Map.of(
            "role", "user",
            "parts", List.of(userPart)
        );

        Map<String, Object> body = Map.of(
            "contents", List.of(systemContent, userContent)
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                GEMINI_URL + geminiApiKey,
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> candidates =
                (List<Map<String, Object>>) responseBody.get("candidates");

            @SuppressWarnings("unchecked")
            Map<String, Object> content =
                (Map<String, Object>) candidates.get(0).get("content");

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parts =
                (List<Map<String, Object>>) content.get("parts");

            String reply = (String) parts.get(0).get("text");

            return new ChatbotResponseDTO(
                reply != null ? reply : "Sin respuesta.",
                resolveRoleName(rolId)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return new ChatbotResponseDTO(
                "Lo siento, hubo un problema al procesar tu consulta. Por favor intenta de nuevo.",
                resolveRoleName(rolId)
            );
        }
    }

    // ──────────────────────────────────────────────────────────────────
    // PROMPT BASE
    // ──────────────────────────────────────────────────────────────────

    private String buildSystemPrompt(RolEnum rol) {

        String base = """
            Eres Turbo Bot, el asistente virtual del taller automotriz TurboMechanics.
            Responde siempre en español, de forma clara y concisa.
            Más abajo encontrarás bloques de "CONTEXTO" con información real y actualizada
            del sistema (servicios, talleres, citas, órdenes, garantías, etc.), ya filtrada
            según el rol y el usuario que te está hablando.
            Usa SIEMPRE esos datos cuando te pregunten por ellos. No inventes datos que no
            estén en el contexto. Si te preguntan algo que no aparece en ningún bloque,
            indica amablemente que deben consultar la sección correspondiente de la plataforma.
            Nunca reveles información de otros usuarios distinta a la que se te entrega en
            el contexto, y nunca uses lenguaje ofensivo ni inapropiado.
            """;

        return switch (rol) {
            case ADMIN -> base + """
                El usuario es un ADMINISTRADOR del taller.
                Puedes ayudarle con:
                - Orientación sobre gestión de talleres, inventario, mecánicos y clientes
                - Buenas prácticas de administración de talleres automotrices
                - Interpretación de métricas y reportes generales (los del bloque de contexto)
                - Resolución de dudas sobre módulos del sistema (citas, órdenes, facturación, repuestos)
                - Gestión de garantías y control de calidad
                Usa un tono profesional y directo.
                """;
            case MECANICO -> base + """
                El usuario es un MECÁNICO del taller.
                Puedes ayudarle con:
                - Consultas técnicas sobre reparación y mantenimiento de vehículos
                - Orientación sobre diagnóstico de fallas comunes
                - Buenas prácticas mecánicas y de seguridad en el taller
                - Sus propias órdenes de trabajo asignadas (incluidas en el contexto)
                - Información general sobre repuestos y su uso
                No proporciones datos de clientes que no estén ligados a sus órdenes asignadas.
                Usa un tono técnico y amigable.
                """;
            case CLIENTE -> base + """
                El usuario es un CLIENTE del taller.
                Puedes ayudarle con:
                - Información sobre los servicios que ofrece el taller, incluyendo sus precios reales
                - Consejos de mantenimiento preventivo para vehículos
                - Sus propias citas, órdenes de trabajo y garantías (incluidas en el contexto)
                - Ubicación y horarios de los talleres
                - Preguntas frecuentes sobre garantías y procesos del taller
                No proporciones información de otros clientes ni datos internos del taller.
                Usa un tono amable, sencillo y cercano.
                """;
        };
    }

    // ──────────────────────────────────────────────────────────────────
    // CONSTRUCCIÓN DE CONTEXTO (varía según el rol)
    // ──────────────────────────────────────────────────────────────────

    private String buildContext(RolEnum rol, Long userId) {
        StringBuilder context = new StringBuilder();

        // Común a los tres roles: catálogo de servicios y talleres.
        context.append(buildServicesContext());
        context.append(buildWorkshopsContext());

        switch (rol) {
            case CLIENTE -> {
                context.append(buildClientSparePartsContext());
                Optional<Users> usuario = (userId != null) ? usersRepository.findById(userId) : Optional.<Users>empty();
                if (usuario.isPresent()) {
                    String cedula = String.valueOf(usuario.get().getIdentification());
                    context.append(buildClientAppointmentsContext(usuario.get().getIdentification()));
                    context.append(buildClientWorkOrdersContext(cedula));
                    context.append(buildClientWarrantiesContext(cedula));
                } else {
                    context.append("\nNo fue posible identificar al cliente para traer sus citas, órdenes o garantías.\n");
                }
            }
            case MECANICO -> {
                Optional<Users> usuario = (userId != null) ? usersRepository.findById(userId) : Optional.<Users>empty();
                if (usuario.isPresent()) {
                    Optional<Mechanic> mecanico = mechanicRepository.findByDocument(
                        Long.valueOf(usuario.get().getIdentification())
                    );
                    if (mecanico.isPresent()) {
                        context.append(buildMechanicWorkOrdersContext(mecanico.get()));
                    } else {
                        context.append("\nNo fue posible encontrar el registro de mecánico asociado a este usuario.\n");
                    }
                }
            }
            case ADMIN -> {
                context.append(buildAdminWorkOrdersSummary());
                context.append(buildAdminMechanicsSummary());
                context.append(buildAdminCriticalStockContext());
                context.append(buildAdminAppointmentsTodayContext());
            }
        }

        return context.toString();
    }

    /**
     * Catálogo real de servicios activos. Disponible para los tres roles.
     */
    private String buildServicesContext() {
        List<ServiceEntity> services = serviceRepository.findByActiveTrue();

        if (services.isEmpty()) {
            return "\nCATÁLOGO DE SERVICIOS:\nActualmente no hay servicios activos registrados.\n";
        }

        String listado = services.stream()
            .map(s -> "- %s: %s (Precio: $%s)".formatted(
                s.getName(),
                s.getDescription(),
                formatPrice(s.getPrice())
            ))
            .collect(Collectors.joining("\n"));

        return """

            CATÁLOGO DE SERVICIOS (datos reales y actualizados del taller):
            %s
            """.formatted(listado);
    }

    /**
     * Talleres activos: nombre, ciudad, dirección, horario. Disponible para los tres roles.
     */
    private String buildWorkshopsContext() {
        List<Workshop> workshops = workshopRepository.findByActiveTrueAndDeletedAtIsNullOrderByNameAsc();

        if (workshops.isEmpty()) {
            return "\nTALLERES:\nActualmente no hay talleres activos registrados.\n";
        }

        String listado = workshops.stream()
            .map(w -> "- %s (%s, %s): %s. Horario: %s".formatted(
                w.getName(),
                w.getCity(),
                w.getAddress(),
                w.getPhone() != null ? "Tel: " + w.getPhone() : "Sin teléfono registrado",
                w.getSchedule() != null ? w.getSchedule() : "No especificado"
            ))
            .collect(Collectors.joining("\n"));

        return """

            TALLERES ACTIVOS:
            %s
            """.formatted(listado);
    }

    /**
     * Citas del cliente que está hablando con el bot (solo las suyas, por cédula).
     */
    private String buildClientAppointmentsContext(Integer identification) {
        List<Appointment> citas = appointmentRepository.findByUsersIdentification(identification);

        if (citas.isEmpty()) {
            return "\nCITAS DEL CLIENTE:\nEl cliente no tiene citas registradas.\n";
        }

        String listado = citas.stream()
            .sorted(Comparator.comparing(Appointment::getDate).reversed())
            .limit(10)
            .map(c -> "- %s %s | Estado: %s | Motivo: %s".formatted(
                c.getDate().format(FECHA),
                c.getTime(),
                c.getStatus(),
                c.getReason() != null ? c.getReason() : "No especificado"
            ))
            .collect(Collectors.joining("\n"));

        return """

            CITAS DEL CLIENTE (las más recientes, máximo 10):
            %s
            """.formatted(listado);
    }

    /**
     * Órdenes de trabajo del cliente que está hablando con el bot (solo las suyas).
     */
    private String buildClientWorkOrdersContext(String clientIdentification) {
        List<WorkOrder> ordenes = workOrderRepository.findByClientidentification(clientIdentification);

        if (ordenes.isEmpty()) {
            return "\nÓRDENES DE TRABAJO DEL CLIENTE:\nEl cliente no tiene órdenes de trabajo registradas.\n";
        }

        String listado = ordenes.stream()
            .sorted(Comparator.comparing(WorkOrder::getDatecreation).reversed())
            .limit(10)
            .map(o -> "- Orden %s | Vehículo: %s %s %s (placa %s) | Estado: %s | Fallas reportadas: %s".formatted(
                o.getNumberorder(),
                o.getVehiclebrand(),
                o.getVehiclemodel(),
                o.getVehicleyear(),
                o.getVehicleplate(),
                o.getStateorder(),
                o.getFailuresreported()
            ))
            .collect(Collectors.joining("\n"));

        return """

            ÓRDENES DE TRABAJO DEL CLIENTE (las más recientes, máximo 10):
            %s
            """.formatted(listado);
    }

    /**
     * Garantías del cliente que está hablando con el bot (solo las suyas, vía sus órdenes).
     */
    private String buildClientWarrantiesContext(String clientIdentification) {
        List<Warranty> garantias = warrantyRepository.findByWorkOrderClientidentificationOrderByCreatedAtDesc(clientIdentification);

        if (garantias.isEmpty()) {
            return "\nGARANTÍAS DEL CLIENTE:\nEl cliente no tiene garantías registradas.\n";
        }

        String listado = garantias.stream()
            .limit(10)
            .map(g -> {
                List<String> partes = new java.util.ArrayList<>();
                if (g.getServiceCoverages() != null) {
                    g.getServiceCoverages().forEach(c -> {
                        if (c.getService() != null) partes.add(c.getService().getName());
                        else if (c.getNameSnapshot() != null) partes.add(c.getNameSnapshot() + " (eliminado)");
                    });
                }
                if (g.getSparePartCoverages() != null) {
                    g.getSparePartCoverages().forEach(c -> {
                        if (c.getSparePart() != null) partes.add(c.getSparePart().getName());
                        else if (c.getNameSnapshot() != null) partes.add(c.getNameSnapshot() + " (eliminado)");
                    });
                }
                String cubre = partes.isEmpty() ? "No especificado" : String.join(", ", partes);
                return "- Comprobante %s | Cubre: %s | Vigencia: %s a %s | Estado: %s".formatted(
                    g.getVoucherNumber() != null ? g.getVoucherNumber() : "Sin comprobante",
                    cubre,
                    g.getStartDate().format(FECHA),
                    g.getEndDate().format(FECHA),
                    g.getStatus()
                );
            })
            .collect(Collectors.joining("\n"));

        return """

            GARANTÍAS DEL CLIENTE (máximo 10):
            %s
            """.formatted(listado);
    }

    /**
     * Órdenes de trabajo asignadas al mecánico que está hablando con el bot.
     */
    private String buildMechanicWorkOrdersContext(Mechanic mecanico) {
        List<WorkOrder> ordenes = workOrderRepository.findByAssignedMechanicIdOrderByDatecreationDesc(mecanico.getId());
        long activas = workOrderRepository.countActiveOrdersByMechanic(mecanico.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("\nÓRDENES DE TRABAJO ASIGNADAS AL MECÁNICO:\n");
        sb.append("Capacidad máxima de órdenes activas: %d | Órdenes activas actuales: %d\n"
            .formatted(mecanico.getMaxOrderCapacity(), activas));

        if (ordenes.isEmpty()) {
            sb.append("Actualmente no tiene órdenes de trabajo asignadas.\n");
            return sb.toString();
        }

        String listado = ordenes.stream()
            .limit(10)
            .map(o -> "- Orden %s | Cliente: %s | Vehículo: %s %s (placa %s) | Estado: %s | Prioridad: %s | Fallas: %s".formatted(
                o.getNumberorder(),
                o.getClientname(),
                o.getVehiclebrand(),
                o.getVehiclemodel(),
                o.getVehicleplate(),
                o.getStateorder(),
                o.getPriority(),
                o.getFailuresreported()
            ))
            .collect(Collectors.joining("\n"));

        sb.append("Últimas órdenes (máximo 10):\n").append(listado).append("\n");
        return sb.toString();
    }

    /**
     * Resumen agregado de órdenes de trabajo por estado, para el administrador.
     */
    private String buildAdminWorkOrdersSummary() {
        List<WorkOrder> todas = workOrderRepository.findAll();

        if (todas.isEmpty()) {
            return "\nRESUMEN DE ÓRDENES DE TRABAJO:\nNo hay órdenes de trabajo registradas.\n";
        }

        Map<WorkOrder.StateOrder, Long> porEstado = todas.stream()
            .collect(Collectors.groupingBy(WorkOrder::getStateorder, Collectors.counting()));

        String resumen = Arrays.stream(WorkOrder.StateOrder.values())
            .map(estado -> "- %s: %d".formatted(estado, porEstado.getOrDefault(estado, 0L)))
            .collect(Collectors.joining("\n"));

        return """

            RESUMEN DE ÓRDENES DE TRABAJO (total: %d):
            %s
            """.formatted(todas.size(), resumen);
    }

    /**
     * Resumen de mecánicos activos y su carga actual, para el administrador.
     */
    private String buildAdminMechanicsSummary() {
        List<Mechanic> mecanicos = mechanicRepository.findByLaborStatus(
            com.proyecto.TurboMechanics.enums.LaborStatus.ACTIVO
        );

        if (mecanicos.isEmpty()) {
            return "\nMECÁNICOS ACTIVOS:\nNo hay mecánicos activos registrados.\n";
        }

        String listado = mecanicos.stream()
            .map(m -> {
                long activas = workOrderRepository.countActiveOrdersByMechanic(m.getId());
                return "- %s (%s) | Órdenes activas: %d/%d".formatted(
                    m.getName(), m.getPosition(), activas, m.getMaxOrderCapacity()
                );
            })
            .collect(Collectors.joining("\n"));

        return """

            MECÁNICOS ACTIVOS Y SU CARGA ACTUAL:
            %s
            """.formatted(listado);
    }

    /**
     * Repuestos con stock crítico o agotado, para el administrador.
     */
    private String buildAdminCriticalStockContext() {
        List<SpareParts> criticos = sparePartsRepository.findStockCritical();

        if (criticos.isEmpty()) {
            return "\nSTOCK DE REPUESTOS:\nNo hay repuestos en estado crítico actualmente.\n";
        }

        String listado = criticos.stream()
            .limit(15)
            .map(r -> "- %s (ref. %s) | Stock actual: %d | Stock mínimo: %d".formatted(
                r.getName(), r.getReference(), r.getStock(), r.getStockMin()
            ))
            .collect(Collectors.joining("\n"));

        return """

            REPUESTOS CON STOCK CRÍTICO O AGOTADO (máximo 15):
            %s
            """.formatted(listado);
    }

    /**
     * Citas programadas para hoy, para el administrador.
     */
    private String buildAdminAppointmentsTodayContext() {
        LocalDate hoy = LocalDate.now();
        List<Appointment> citasHoy = appointmentRepository.findByDate(hoy);

        if (citasHoy.isEmpty()) {
            return "\nCITAS DE HOY (%s):\nNo hay citas programadas para hoy.\n".formatted(hoy.format(FECHA));
        }

        String listado = citasHoy.stream()
            .sorted(Comparator.comparing(Appointment::getTime))
            .map(c -> "- %s | Estado: %s | Motivo: %s".formatted(
                c.getTime(), c.getStatus(), c.getReason() != null ? c.getReason() : "No especificado"
            ))
            .collect(Collectors.joining("\n"));

        return """

            CITAS DE HOY (%s):
            %s
            """.formatted(hoy.format(FECHA), listado);
    }

    /**
     * Repuestos disponibles para el cliente que está hablando con el bot.
     */
    private String buildClientSparePartsContext() {
    List<SpareParts> repuestos = sparePartsRepository.findAll().stream()
        .filter(r -> r.getStock() > 0)
        .toList();

    if (repuestos.isEmpty()) {
        return "\nTIENDA DE REPUESTOS:\nActualmente no hay repuestos disponibles.\n";
    }

    String listado = repuestos.stream()
        .map(r -> "- %s (ref. %s) | Categoría: %s | Precio: $%s | Stock: %d".formatted(
            r.getName(),
            r.getReference(),
            r.getCategory(),
            formatPrice(r.getPrice()),
            r.getStock()
        ))
        .collect(Collectors.joining("\n"));

    return """

        TIENDA DE REPUESTOS DISPONIBLES:
        %s
        """.formatted(listado);
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) return "No disponible";
        return price.setScale(0, java.math.RoundingMode.HALF_UP).toString();
    }

    private boolean containsBlockedWord(String text) {
        String normalized = text.toLowerCase()
            .replaceAll("[áàä]", "a")
            .replaceAll("[éèë]", "e")
            .replaceAll("[íìï]", "i")
            .replaceAll("[óòö]", "o")
            .replaceAll("[úùü]", "u")
            .replaceAll("[ñ]", "n");

        String[] words = normalized.split("[\\s.,!?;:\"'()]+");
        for (String word : words) {
            if (BLOCKED_WORDS.contains(word)) return true;
        }
        return false;
    }

    private RolEnum getRolEnum(Long rolId) {
        if (rolId == null) return RolEnum.CLIENTE;
        for (RolEnum rol : RolEnum.values()) {
            if (rol.getId().equals(rolId)) return rol;
        }
        return RolEnum.CLIENTE;
    }

    private String resolveRoleName(Long rolId) {
        return getRolEnum(rolId).name();
    }
}