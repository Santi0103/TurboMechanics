package com.proyecto.TurboMechanics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.proyecto.TurboMechanics.security.RoleInterceptor;

import lombok.RequiredArgsConstructor;

// Configuracion adicional para registrar interceptores en spring
@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer{
    private final RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        // Registramos el interceptor para que se ejecute en todas las rutas
        registry.addInterceptor(roleInterceptor);
    }
}
