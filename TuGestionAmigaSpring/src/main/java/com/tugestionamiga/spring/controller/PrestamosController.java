package com.tugestionamiga.spring.controller;

import com.tugestionamiga.spring.repository.LibroRepository;
import com.tugestionamiga.spring.repository.PrestamoRepository;
import com.tugestionamiga.spring.repository.UsuarioRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador del módulo de préstamos.
 *
 * <p>
 * Reglas principales:
 * </p>
 *
 * <ul>
 *   <li><strong>ADMIN</strong>: ve todos los préstamos, puede registrar devoluciones y eliminar.</li>
 *   <li><strong>USUARIO</strong>: ve solo sus préstamos y registra préstamos para sí mismo.</li>
 * </ul>
 *
 * <p>
 * Validación de fechas:
 * </p>
 *
 * <ul>
 *   <li>La fecha de préstamo debe ser la fecha actual.</li>
 *   <li>La fecha de devolución no puede ser anterior a la fecha de préstamo.</li>
 * </ul>
 */
@Controller
public class PrestamosController {

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    public PrestamosController(UsuarioRepository usuarioRepository, LibroRepository libroRepository, PrestamoRepository prestamoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.prestamoRepository = prestamoRepository;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private Integer getIdUsuarioSesion(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        Optional<Integer> id = usuarioRepository.findIdByCorreo(authentication.getName());
        return id.orElse(null);
    }

    @GetMapping("/prestamos")
    public String prestamos(Authentication authentication, Model model) {
        boolean esAdmin = isAdmin(authentication);
        model.addAttribute("esAdmin", esAdmin);

        model.addAttribute("libros", libroRepository.findAll());

        if (esAdmin) {
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("prestamos", prestamoRepository.findAll());
        } else {
            Integer idUsuario = getIdUsuarioSesion(authentication);
            if (idUsuario == null) {
                model.addAttribute("prestamos", java.util.List.of());
            } else {
                model.addAttribute("prestamos", prestamoRepository.findByUsuario(idUsuario));
            }
        }

        return "prestamos";
    }

    @PostMapping("/prestamos")
    public String prestamosPost(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "idUsuario", required = false) Integer idUsuario,
            @RequestParam(name = "idLibro", required = false) Integer idLibro,
            @RequestParam(name = "fechaPrestamo", required = false) String fechaPrestamo,
            @RequestParam(name = "fechaDevolucion", required = false) String fechaDevolucion,
            @RequestParam(name = "idPrestamo", required = false) Integer idPrestamo,
            @RequestParam(name = "id", required = false) Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        String accionFinal = (accion == null || accion.trim().isEmpty()) ? "registrarPrestamo" : accion.trim();
        boolean esAdmin = isAdmin(authentication);

        try {
            if ("registrarPrestamo".equalsIgnoreCase(accionFinal)) {
                int idUsuarioFinal;
                if (esAdmin) {
                    if (idUsuario == null) {
                        throw new IllegalArgumentException("Debes seleccionar un usuario.");
                    }
                    idUsuarioFinal = idUsuario;
                } else {
                    Integer sesion = getIdUsuarioSesion(authentication);
                    if (sesion == null) {
                        throw new IllegalArgumentException("Sesión no encontrada.");
                    }
                    idUsuarioFinal = sesion;
                }

                if (idLibro == null) {
                    throw new IllegalArgumentException("Debes seleccionar un libro.");
                }

                LocalDate fPrestamo = LocalDate.parse(fechaPrestamo);
                LocalDate fDevolucion = LocalDate.parse(fechaDevolucion);

                LocalDate hoy = LocalDate.now();
                if (!fPrestamo.equals(hoy)) {
                    throw new IllegalArgumentException("La fecha de préstamo debe ser la fecha actual.");
                }
                if (fDevolucion.isBefore(fPrestamo)) {
                    throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a la fecha de préstamo.");
                }

                int newId = prestamoRepository.registrarPrestamo(idUsuarioFinal, idLibro, fPrestamo, fDevolucion);

                if (newId > 0) {
                    redirectAttributes.addFlashAttribute("mensaje", "Préstamo registrado con ID: " + newId);
                } else {
                    redirectAttributes.addFlashAttribute("error", "No se pudo registrar el préstamo. Verifica que el libro exista y esté disponible.");
                }

            } else if ("registrarDevolucion".equalsIgnoreCase(accionFinal)) {
                if (!esAdmin) {
                    redirectAttributes.addFlashAttribute("error", "Acceso restringido: solo un administrador puede registrar devoluciones.");
                    return "redirect:/prestamos";
                }
                if (idPrestamo == null) {
                    throw new IllegalArgumentException("Debes indicar el ID del préstamo.");
                }

                boolean ok = prestamoRepository.registrarDevolucion(idPrestamo);
                redirectAttributes.addFlashAttribute("mensaje", ok ? "Devolución registrada." : "No se pudo registrar la devolución.");

            } else if ("eliminar".equalsIgnoreCase(accionFinal)) {
                if (!esAdmin) {
                    redirectAttributes.addFlashAttribute("error", "Acceso restringido: solo un administrador puede eliminar préstamos.");
                    return "redirect:/prestamos";
                }
                Integer idFinal = (id != null) ? id : idPrestamo;
                if (idFinal == null) {
                    throw new IllegalArgumentException("Debes indicar el ID del préstamo.");
                }

                boolean ok = prestamoRepository.delete(idFinal);
                redirectAttributes.addFlashAttribute("mensaje", ok ? "Préstamo eliminado." : "No se pudo eliminar el préstamo.");
            }

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar préstamo: " + ex.getMessage());
        }

        return "redirect:/prestamos";
    }
}
