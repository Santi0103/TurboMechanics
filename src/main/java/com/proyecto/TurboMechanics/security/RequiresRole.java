package com.proyecto.TurboMechanics.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.proyecto.TurboMechanics.entity.RolType;

// Define donde se puede usar la anotación
@Target(ElementType.METHOD)
// RUNTIME: disponible en ejecucion, en todo momento (esto es necesario para que el interceptor lo lea)
@Retention(RetentionPolicy.RUNTIME)
// Anotacion personalizada
public @interface RequiresRole {
    RolType[] value();
}