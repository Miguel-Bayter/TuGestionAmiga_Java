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
 *
 * <p>
 * Otros puntos del flujo:
 * </p>
 *
 * <ul>
 *   <li>Se puede prestar una <strong>cantidad</strong> de ejemplares del mismo libro (si hay stock).</li>
 *   <li>Si llega {@code idLibro} por query param (desde el listado de libros), el selector queda autollenado.</li>
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

    /**
     * Determina si el usuario autenticado tiene el rol ADMIN.
     *
     * <p>
     * Se utiliza para habilitar funciones administrativas:
     * ver todos los préstamos, registrar devoluciones y eliminar.
     * </p>
     */
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /**
     * Convierte el correo del usuario autenticado en {@code id_usuario}.
     *
     * <p>
     * Esto es necesario porque el login trabaja con correo, pero el préstamo se relaciona por id.
     * </p>
     */
    private Integer getIdUsuarioSesion(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return null;
        }
        Optional<Integer> id = usuarioRepository.findIdByCorreo(authentication.getName());
        return id.orElse(null);
    }

    /**
     * Renderiza la vista de préstamos.
     *
     * <p>
     * Carga libros para el selector y luego:
     * </p>
     *
     * <ul>
     *   <li>ADMIN: carga usuarios + listado completo.</li>
     *   <li>USUARIO: carga solo los préstamos del usuario en sesión.</li>
     * </ul>
     *
     * <p>
     * Si el usuario entra desde el listado de libros, se puede enviar {@code idLibro} para que el
     * formulario quede con el libro preseleccionado.
     * </p>
     */
    @GetMapping("/prestamos")
    public String prestamos(
            @RequestParam(name = "idLibro", required = false) Integer idLibro,
            Authentication authentication,
            Model model
    ) {
        boolean esAdmin = isAdmin(authentication);
        model.addAttribute("esAdmin", esAdmin);

        // Se envía la fecha actual a la vista para evitar expresiones Thymeleaf con T(...)
        // (en algunos entornos eso puede generar error 500 por restricciones de seguridad).
        model.addAttribute("hoy", LocalDate.now());

        // Se usa para autollenar el selector de libros cuando el usuario viene desde el listado.
        model.addAttribute("idLibroSeleccionado", idLibro);

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

    /**
     * Procesa acciones del módulo de préstamos.
     *
     * <p>
     * Incluye:
     * </p>
     *
     * <ul>
     *   <li>{@code registrarPrestamo}: descuenta stock, guarda préstamo y valida fechas.</li>
     *   <li>{@code registrarDevolucion}: cambia a DEVUELTO y repone stock (solo ADMIN).</li>
     *   <li>{@code eliminar}: operación administrativa (solo ADMIN).</li>
     * </ul>
     */
    @PostMapping("/prestamos")
    public String prestamosPost(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "idUsuario", required = false) Integer idUsuario,
            @RequestParam(name = "idLibro", required = false) Integer idLibro,
            @RequestParam(name = "fechaPrestamo", required = false) String fechaPrestamo,
            @RequestParam(name = "fechaDevolucion", required = false) String fechaDevolucion,
            @RequestParam(name = "cantidad", required = false) Integer cantidad,
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

                // Validación básica para evitar null y errores de parseo en fechas.
                if (fechaPrestamo == null || fechaPrestamo.trim().isEmpty()) {
                    throw new IllegalArgumentException("Debes indicar la fecha de préstamo.");
                }
                if (fechaDevolucion == null || fechaDevolucion.trim().isEmpty()) {
                    throw new IllegalArgumentException("Debes indicar la fecha de devolución.");
                }

                LocalDate fPrestamo = LocalDate.parse(fechaPrestamo);
                LocalDate hoy = LocalDate.now();
                // Regla solicitada: la fecha de préstamo no puede ser inferior ni superior a la fecha actual.
                // En otras palabras, debe ser exactamente hoy.
                if (!fPrestamo.equals(hoy)) {
                    throw new IllegalArgumentException("La fecha de préstamo debe ser la fecha actual (" + hoy + ").");
                }

                LocalDate fDevolucion = LocalDate.parse(fechaDevolucion);
                if (fDevolucion.isBefore(fPrestamo)) {
                    throw new IllegalArgumentException("La fecha de devolución no puede ser anterior a la fecha de préstamo.");
                }

                // Cantidad de ejemplares a prestar del mismo libro.
                // Si no viene del formulario, se asume 1 para mantener compatibilidad.
                int cantidadFinal = (cantidad == null) ? 1 : cantidad;
                if (cantidadFinal <= 0) {
                    throw new IllegalArgumentException("La cantidad a prestar debe ser mayor o igual a 1.");
                }

                int newId = prestamoRepository.registrarPrestamo(idUsuarioFinal, idLibro, fPrestamo, fDevolucion, cantidadFinal);

                if (newId > 0) {
                    if (cantidadFinal == 1) {
                        redirectAttributes.addFlashAttribute("mensaje", "Préstamo registrado con ID: " + newId);
                    } else {
                        redirectAttributes.addFlashAttribute("mensaje", "Préstamos registrados: " + cantidadFinal + " (último ID: " + newId + ")");
                    }
                } else {
                    boolean noHayStock = libroRepository.findById(idLibro)
                            .map(l -> l.getStock() <= 0)
                            .orElse(true);

                    if (noHayStock) {
                        redirectAttributes.addFlashAttribute("error", "No hay libros disponibles");
                    } else {
                        redirectAttributes.addFlashAttribute("error", "No se pudo registrar el préstamo.");
                    }
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
