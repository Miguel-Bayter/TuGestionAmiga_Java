package com.tugestionamiga.spring.controller;

import com.tugestionamiga.spring.repository.CompraRepository;
import com.tugestionamiga.spring.repository.LibroRepository;
import com.tugestionamiga.spring.repository.UsuarioRepository;
import java.math.BigDecimal;
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
 * Controlador del módulo de compras.
 *
 * <p>
 * Permite registrar compras y listar compras.
 * </p>
 *
 * <p>
 * Reglas importantes del registro:
 * </p>
 *
 * <ul>
 *   <li>La <strong>fecha de compra</strong> se fija con la fecha actual (no se permite cambiarla).</li>
 *   <li>El <strong>precio</strong> no lo escribe el usuario: se toma del <code>valor</code> del libro (catálogo).</li>
 *   <li>Se puede comprar una <strong>cantidad</strong> de ejemplares del mismo libro en una sola operación.</li>
 * </ul>
 *
 * <ul>
 *   <li><strong>ADMIN</strong>: puede registrar compras para cualquier usuario y eliminar compras.</li>
 *   <li><strong>USUARIO</strong>: registra compras para sí mismo y ve solo sus compras.</li>
 * </ul>
 *
 * <p>
 * El registro de compra usa una transacción que inserta en {@code compra} y descuenta stock del libro.
 * </p>
 */
@Controller
public class ComprasController {

    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;
    private final CompraRepository compraRepository;

    public ComprasController(UsuarioRepository usuarioRepository, LibroRepository libroRepository, CompraRepository compraRepository) {
        this.usuarioRepository = usuarioRepository;
        this.libroRepository = libroRepository;
        this.compraRepository = compraRepository;
    }

    /**
     * Determina si el usuario autenticado tiene el rol ADMIN.
     *
     * <p>
     * Se usa para decidir qué datos se muestran y qué acciones se permiten (por ejemplo, eliminar).
     * </p>
     */
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /**
     * Obtiene el id del usuario en sesión a partir del correo (username) de Spring Security.
     *
     * <p>
     * Existe porque el sistema autentica por correo, pero las tablas de negocio relacionan por
     * {@code id_usuario}.
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
     * Renderiza la vista de compras.
     *
     * <p>
     * ADMIN ve listado completo y selector de usuarios. Un usuario normal solo ve sus propias compras.
     * También se cargan libros para poblar el selector.
     * </p>
     *
     * <p>
     * Además, si llega el parámetro {@code idLibro} (por ejemplo, desde la vista de libros),
     * se envía a la vista como {@code idLibroSeleccionado} para dejar el selector autollenado.
     * </p>
     */
    @GetMapping("/compras")
    public String compras(
            @RequestParam(name = "idLibro", required = false) Integer idLibro,
            Authentication authentication,
            Model model
    ) {
        boolean esAdmin = isAdmin(authentication);
        model.addAttribute("esAdmin", esAdmin);

        // Se envía la fecha actual para fijarla en el formulario (min/max/value).
        model.addAttribute("hoy", LocalDate.now());

        // Se usa para autollenar el selector de libros cuando se entra desde el listado.
        model.addAttribute("idLibroSeleccionado", idLibro);

        model.addAttribute("libros", libroRepository.findAll());

        if (esAdmin) {
            model.addAttribute("usuarios", usuarioRepository.findAll());
            model.addAttribute("compras", compraRepository.findAll());
        } else {
            Integer idUsuario = getIdUsuarioSesion(authentication);
            if (idUsuario == null) {
                model.addAttribute("compras", java.util.List.of());
            } else {
                model.addAttribute("compras", compraRepository.findByUsuario(idUsuario));
            }
        }

        return "compras";
    }

    /**
     * Procesa acciones del módulo de compras.
     *
     * <p>
     * Acciones principales:
     * </p>
     *
     * <ul>
     *   <li>{@code registrarCompra}: inserta compra y descuenta stock (transacción).</li>
     *   <li>{@code eliminar}: operación administrativa, restringida a ADMIN.</li>
     * </ul>
     */
    @PostMapping("/compras")
    public String comprasPost(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "idUsuario", required = false) Integer idUsuario,
            @RequestParam(name = "idLibro", required = false) Integer idLibro,
            @RequestParam(name = "fechaCompra", required = false) String fechaCompra,
            @RequestParam(name = "precio", required = false) String precio,
            @RequestParam(name = "cantidad", required = false) Integer cantidad,
            @RequestParam(name = "id", required = false) Integer id,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        String accionFinal = (accion == null || accion.trim().isEmpty()) ? "registrarCompra" : accion.trim();
        boolean esAdmin = isAdmin(authentication);

        try {
            if ("registrarCompra".equalsIgnoreCase(accionFinal)) {
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

                // Si no viene cantidad desde el formulario, se usa 1 para no romper el flujo actual.
                int cantidadFinal = (cantidad == null) ? 1 : cantidad;
                if (cantidadFinal <= 0) {
                    throw new IllegalArgumentException("La cantidad a comprar debe ser mayor o igual a 1.");
                }

                // Regla: la fecha de compra es la fecha del día en que se realiza la compra.
                // Aunque el formulario sea manipulado, el servidor fija el valor.
                LocalDate fCompra = LocalDate.now();

                // Regla: el usuario no define el precio. Se toma el valor del libro desde el catálogo.
                // Con esto evitamos que el usuario cambie el monto en el navegador.
                BigDecimal valorLibro = libroRepository.findById(idLibro)
                        .map(l -> l.getValor() == null ? BigDecimal.ZERO : l.getValor())
                        .orElse(BigDecimal.ZERO);

                // Se guarda el precio unitario en cada registro de compra.
                // El total se calcula como: valorLibro * cantidadFinal.
                BigDecimal precioBd = valorLibro;

                int newId = compraRepository.registrarCompra(idUsuarioFinal, idLibro, fCompra, precioBd, cantidadFinal);

                if (newId > 0) {
                    if (cantidadFinal == 1) {
                        redirectAttributes.addFlashAttribute("mensaje", "Compra registrada con ID: " + newId);
                    } else {
                        redirectAttributes.addFlashAttribute("mensaje", "Compras registradas: " + cantidadFinal + " (último ID: " + newId + ")");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("error", "No se pudo registrar la compra. Verifica que el libro exista y tenga stock.");
                }

            } else if ("eliminar".equalsIgnoreCase(accionFinal)) {
                if (!esAdmin) {
                    redirectAttributes.addFlashAttribute("error", "Acceso restringido: solo un administrador puede eliminar compras.");
                    return "redirect:/compras";
                }
                if (id == null) {
                    throw new IllegalArgumentException("Debes indicar el ID de la compra.");
                }
                boolean ok = compraRepository.delete(id);
                redirectAttributes.addFlashAttribute("mensaje", ok ? "Compra eliminada." : "No se pudo eliminar la compra.");
            }

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar compra: " + ex.getMessage());
        }

        return "redirect:/compras";
    }
}
