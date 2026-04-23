package com.proyecto.TurboMechanics.security;

import java.util.Arrays;

import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RoleInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // verifica que el handler sea un método del controller y no cualquier otra cosa
        if (!(handler instanceof HandlerMethod method)) {
            // if(handler == null || !(handler instanceof HandlerMethod))
            return true; // Si no es un endpoint no nos importa
        }

        // Busca la anotacion @RequiresRole en el método
        RequiresRole annotation = method.getMethodAnnotation(RequiresRole.class);

        // Si no está en el metodo, la busca en la clase (controller)
        if (annotation == null) {
            annotation = method.getBeanType().getAnnotation(RequiresRole.class);
        }

        // Si no hay anotación, el endpoint es público (no requiere rol)
        if (annotation == null) {
            return true; // Tampoco nos importa
        }

        Object rol = request.getAttribute("rolId");

        if (!(rol instanceof Long rolId)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Usuario no autenticado\"}");
            return false; // bloquear la peticion
        }

        // Verifica si el rol del usuario esta dentro de los roles permitidos
        boolean hasRole = Arrays.stream(annotation.value()).anyMatch(role -> role.getId() == rolId);

        // Si no tiene permisos, responde 403
        if (!hasRole) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"No tienes permisos para realizar esta accion\"}");
            return false;
        }

        // Si pasa todas las validaciones, permite continuar al controller
        return true;
    }
}
