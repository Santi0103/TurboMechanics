package com.proyecto.TurboMechanics.filter;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.proyecto.TurboMechanics.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtValidationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Header Authorization is missing in the request\"}");
            return;
        }

        String token = authHeader.substring(7);
        try {
            if (jwtService.isTokenValid(token)) {
                String username = jwtService.extractUsername(token);
                Long userId = jwtService.extractUserId(token);
                Long rolId = jwtService.extractRolId(token);

                request.setAttribute("username", username);
                request.setAttribute("userId", userId);
                request.setAttribute("rolId", rolId);

                filterChain.doFilter(request, response);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Validation Failed\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Validation failed\"}");
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        if (method.equals("OPTIONS")) {
            return true;
        }
        return path.equals("/auth/login") ||
                path.equals("/auth/register") ||
                path.equals("/auth/refreshToken") ||
                path.equals("/auth/forgot-password") ||
                path.equals("/auth/validate-code") ||
                path.equals("/auth/reset-password") ||
                path.equals("/admin/catalogo/public/servicios") ||
                path.startsWith("/presupuestos/response") || 
                path.startsWith("/presupuestos/publico") || 
                path.startsWith("/files/") ||
                path.equals("/reviews/public") ||
                path.equals("/payments/webhook");
    }
}