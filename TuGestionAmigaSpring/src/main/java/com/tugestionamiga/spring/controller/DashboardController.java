package com.tugestionamiga.spring.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador del dashboard.
 *
 * <p>
 * Esta vista es una pantalla mínima para verificar que:
 * </p>
 *
 * <ul>
 *   <li>Spring Security redirige correctamente al iniciar sesión.</li>
 *   <li>La ruta queda protegida y no permite acceso anónimo.</li>
 *   <li>El usuario autenticado y sus roles se pueden mostrar en la vista.</li>
 * </ul>
 */
@Controller
public class DashboardController {

    @GetMapping({"/", "/dashboard"})
    public String dashboard() {
        return "dashboard";
    }
}
