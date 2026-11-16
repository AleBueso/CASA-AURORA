package com.casaaurora.modelo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Política de acceso de una tabla. H2 no tiene RLS (Row Level Security) como Postgres,
 * así que la política se aplica en la aplicación: Spring Security solo deja leer o
 * escribir estos datos a usuarios con rol ADMIN (ver SecurityConfig).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PoliticaAcceso {
    String[] value();
}