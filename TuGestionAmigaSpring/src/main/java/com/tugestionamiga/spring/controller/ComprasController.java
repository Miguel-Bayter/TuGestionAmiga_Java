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

    @GetMapping("/compras")
    public String compras(Authentication authentication, Model model) {
        boolean esAdmin = isAdmin(authentication);
        model.addAttribute("esAdmin", esAdmin);

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

    @PostMapping("/compras")
    public String comprasPost(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "idUsuario", required = false) Integer idUsuario,
            @RequestParam(name = "idLibro", required = false) Integer idLibro,
            @RequestParam(name = "fechaCompra", required = false) String fechaCompra,
            @RequestParam(name = "precio", required = false) String precio,
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

                LocalDate fCompra = LocalDate.parse(fechaCompra);
                BigDecimal precioBd = new BigDecimal(precio);

                int newId = compraRepository.registrarCompra(idUsuarioFinal, idLibro, fCompra, precioBd);

                if (newId > 0) {
                    redirectAttributes.addFlashAttribute("mensaje", "Compra registrada con ID: " + newId);
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
