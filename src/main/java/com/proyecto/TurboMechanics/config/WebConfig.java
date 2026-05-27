package com.proyecto.TurboMechanics.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.proyecto.TurboMechanics.security.RoleInterceptor;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final RoleInterceptor roleInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor);
    }

    /**
     * Expone la carpeta uploads/evidencias/ como recursos estáticos accesibles
     * en la URL: GET http://localhost:9090/files/evidencias/**
     *
     * Esto permite que el frontend cargue imágenes con una URL directa
     * sin necesidad de un endpoint adicional.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/files/evidencias/**")
                .addResourceLocations("file:uploads/evidencias/");
    }
}