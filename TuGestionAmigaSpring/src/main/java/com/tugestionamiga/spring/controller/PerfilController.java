package com.tugestionamiga.spring.controller;

import com.tugestionamiga.spring.repository.CompraRepository;
import com.tugestionamiga.spring.repository.PrestamoRepository;
import com.tugestionamiga.spring.repository.UsuarioRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador del apartado "Perfil".
 *
 * <p>
 * Esta pantalla muestra un resumen del usuario autenticado y sus movimientos:
 * </p>
 *
 * <ul>
 *   <li>Compras realizadas (tabla {@code compra}).</li>
 *   <li>Préstamos del usuario (tabla {@code prestamo}), incluyendo registros DEVUELTO.</li>
 * </ul>
 *
 * <p>
 * El {@code id_usuario} se obtiene a partir del correo de la sesión (Spring Security).
 * </p>
 */
@Controller
public class PerfilController {

    private final UsuarioRepository usuarioRepository;
    private final CompraRepository compraRepository;
    private final PrestamoRepository prestamoRepository;

    public PerfilController(UsuarioRepository usuarioRepository, CompraRepository compraRepository, PrestamoRepository prestamoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.compraRepository = compraRepository;
        this.prestamoRepository = prestamoRepository;
    }

    /**
     * Renderiza la vista de perfil del usuario en sesión.
     */
    @GetMapping("/perfil")
    public String perfil(Authentication authentication, Model model) {
        String correo = (authentication == null) ? null : authentication.getName();
        if (correo == null) {
            return "redirect:/login";
        }

        Optional<Integer> idUsuarioOpt = usuarioRepository.findIdByCorreo(correo);
        if (idUsuarioOpt.isEmpty()) {
            model.addAttribute("error", "No se encontró el usuario en la base de datos.");
            return "perfil";
        }

        int idUsuario = idUsuarioOpt.get();
        model.addAttribute("usuario", usuarioRepository.findById(idUsuario).orElse(null));
        model.addAttribute("compras", compraRepository.findByUsuarioWithLibro(idUsuario));
        model.addAttribute("prestamos", prestamoRepository.findByUsuarioWithLibro(idUsuario));

        return "perfil";
    }
}
