package com.proyecto.TurboMechanics.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WhatsAppConfig {

    /** URL base del microservicio Node.js de WhatsApp. */
    @Value("${whatsapp.service.url}")
    private String serviceUrl;

    /** Token de autenticación*/
    @Value("${whatsapp.service.token}")
    private String serviceToken;

    /** ID de la sesión de WhatsApp */
    @Value("${whatsapp.session.default}")
    private String defaultSession;

    /**
     * Registra un RestTemplatebcomo bean de Spring para realizar peticiones HTTP al microservicio de WhatsApp.
     * @return instancia de RestTemplate
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**Retorna la URL base del microservicio de WhatsApp */
    public String getServiceUrl() { return serviceUrl; }

    /** Retorna el token de autenticación para el microservicio de WhatsApp.*/
    public String getServiceToken() { return serviceToken; }

    /** Retorna el ID de la sesión de WhatsApp usada por defecto */
    public String getDefaultSession() { return defaultSession; }
}