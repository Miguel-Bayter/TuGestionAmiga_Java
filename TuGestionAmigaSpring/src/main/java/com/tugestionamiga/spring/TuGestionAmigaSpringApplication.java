package com.tugestionamiga.spring;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del módulo stand-alone.
 *
 * <p>
 * Al ejecutarse, Spring Boot levanta un servidor embebido (Tomcat embebido por defecto) y publica
 * las rutas definidas en los controllers.
 * </p>
 */
@SpringBootApplication
public class TuGestionAmigaSpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(TuGestionAmigaSpringApplication.class, args);
    }
}
