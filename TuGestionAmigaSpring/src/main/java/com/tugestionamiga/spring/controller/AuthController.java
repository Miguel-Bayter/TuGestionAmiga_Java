package com.tugestionamiga.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de autenticación.
 *
 * <p>
 * Este controlador solo sirve la vista de login. El procesamiento del formulario (POST /login)
 * se maneja directamente por Spring Security.
 * </p>
 */
@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
