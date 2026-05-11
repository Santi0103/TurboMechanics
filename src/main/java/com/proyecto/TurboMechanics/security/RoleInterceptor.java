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

        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }

        RequiresRole annotation = method.getMethodAnnotation(RequiresRole.class);

        if (annotation == null) {
            annotation = method.getBeanType().getAnnotation(RequiresRole.class);
        }

        if (annotation == null) {
            return true;
        }

        Object rol = request.getAttribute("rolId");

        // ✅ Fix — acepta Integer y Long que puede devolver el JWT
        if (rol == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Usuario no autenticado\"}");
            return false;
        }

        Long rolId = ((Number) rol).longValue(); // ✅ convierte sin importar si es Integer o Long

        // ✅ Fix — .equals() en vez de == para comparar objetos Long
        boolean hasRole = Arrays.stream(annotation.value())
                .anyMatch(role -> role.getId().equals(rolId));

        if (!hasRole) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"No tienes permisos para realizar esta accion\"}");
            return false;
        }

        return true;
    }
}