package com.tugestionamiga.spring.controller;

import java.sql.Types;
import java.util.Objects;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    private final JdbcTemplate jdbcTemplate;

    public AuthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerPost(
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "correo", required = false) String correo,
            @RequestParam(name = "contrasena", required = false) String contrasena,
            @RequestParam(name = "contrasena2", required = false) String contrasena2,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String nombreTrim = nombre == null ? null : nombre.trim();
        String correoTrim = correo == null ? null : correo.trim();
        String contrasenaTrim = contrasena == null ? null : contrasena.trim();
        String contrasena2Trim = contrasena2 == null ? null : contrasena2.trim();

        try {
            if (nombreTrim == null || nombreTrim.isEmpty()
                    || correoTrim == null || correoTrim.isEmpty()
                    || contrasenaTrim == null || contrasenaTrim.isEmpty()
                    || contrasena2Trim == null || contrasena2Trim.isEmpty()) {
                throw new IllegalArgumentException("Debes completar todos los campos.");
            }

            if (!Objects.equals(contrasenaTrim, contrasena2Trim)) {
                throw new IllegalArgumentException("Las contraseñas no coinciden.");
            }

            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM usuario WHERE correo = ?",
                    Integer.class,
                    correoTrim
            );

            if (count != null && count > 0) {
                throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
            }

            jdbcTemplate.update(
                    "INSERT INTO usuario (nombre, correo, `contraseña`, id_rol) VALUES (?, ?, ?, ?)",
                    new Object[]{nombreTrim, correoTrim, contrasenaTrim, null},
                    new int[]{Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER}
            );

            redirectAttributes.addFlashAttribute("mensaje", "Registro exitoso. Ya puedes iniciar sesión.");
            return "redirect:/login";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("nombre", nombreTrim);
            model.addAttribute("correo", correoTrim);
            return "register";
        } catch (Exception ex) {
            model.addAttribute("error", "Ocurrió un error al registrarse: " + ex.getMessage());
            model.addAttribute("nombre", nombreTrim);
            model.addAttribute("correo", correoTrim);
            return "register";
        }
    }
}
