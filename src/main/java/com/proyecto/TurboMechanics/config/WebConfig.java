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

        registry.addInterceptor(roleInterceptor);
    }
}
