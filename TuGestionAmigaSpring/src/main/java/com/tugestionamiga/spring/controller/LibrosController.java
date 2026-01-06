package com.tugestionamiga.spring.controller;

import com.tugestionamiga.spring.model.Libro;
import com.tugestionamiga.spring.repository.CategoriaRepository;
import com.tugestionamiga.spring.repository.LibroRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador del módulo de libros.
 *
 * <ul>
 *   <li><strong>ADMIN</strong>: puede crear, editar, actualizar y eliminar libros.</li>
 *   <li><strong>USUARIO</strong>: solo puede listar (lectura).</li>
 * </ul>
 *
 * <p>
 * Nota: en esta versión, la disponibilidad no se edita manualmente; se deriva de {@code stock > 0}.
 * </p>
 *
 * <p>
 * También se maneja el campo {@code valor}:
 * </p>
 *
 * <ul>
 *   <li>Representa el precio unitario del libro en el catálogo.</li>
 *   <li>Se define desde administración (CRUD de Libros).</li>
 *   <li>En el módulo de compras se usa para calcular el total: <code>valor * cantidad</code>.</li>
 * </ul>
 */
@Controller
public class LibrosController {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;

    public LibrosController(LibroRepository libroRepository, CategoriaRepository categoriaRepository) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
    }

    /**
     * Determina si el usuario autenticado tiene rol ADMIN.
     *
     * <p>
     * Existe para que el controller pueda restringir acciones sensibles sin duplicar lógica.
     * </p>
     */
    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    /**
     * Renderiza la vista de libros.
     *
     * <p>
     * Siempre carga el listado y las categorías. Si se entra en modo edición, se valida que sea ADMIN.
     * </p>
     */
    @GetMapping("/libros")
    public String libros(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "id", required = false) Integer id,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        String accionFinal = (accion == null || accion.trim().isEmpty()) ? "listar" : accion.trim();
        boolean esAdmin = isAdmin(authentication);
        model.addAttribute("esAdmin", esAdmin);

        model.addAttribute("categorias", categoriaRepository.findAll());

        if ("editar".equalsIgnoreCase(accionFinal) && id != null) {
            if (!esAdmin) {
                redirectAttributes.addFlashAttribute("error", "Acceso restringido: solo un administrador puede editar libros.");
                return "redirect:/libros?accion=listar";
            }
            Optional<Libro> libroEdit = libroRepository.findById(id);
            libroEdit.ifPresent(l -> model.addAttribute("libroEdit", l));
        }

        model.addAttribute("libros", libroRepository.findAll());
        return "libros";
    }

    /**
     * Procesa acciones del CRUD de libros.
     *
     * <p>
     * Todas las acciones están restringidas a ADMIN porque afectan el catálogo.
     * La disponibilidad se deriva de stock y no se recibe como input manual.
     * </p>
     */
    @PostMapping("/libros")
    public String librosPost(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "id", required = false) Integer id,
            @RequestParam(name = "titulo", required = false) String titulo,
            @RequestParam(name = "autor", required = false) String autor,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "stock", required = false) Integer stock,
            @RequestParam(name = "valor", required = false) String valor,
            @RequestParam(name = "idCategoria", required = false) String idCategoria,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        boolean esAdmin = isAdmin(authentication);
        if (!esAdmin) {
            redirectAttributes.addFlashAttribute("error", "Acceso restringido: solo un administrador puede modificar libros.");
            return "redirect:/libros?accion=listar";
        }

        String accionFinal = (accion == null || accion.trim().isEmpty()) ? "crear" : accion.trim();

        try {
            if ("crear".equalsIgnoreCase(accionFinal)) {
                Libro l = buildLibro(id, titulo, autor, descripcion, stock, valor, idCategoria, false);
                int newId = libroRepository.create(l);
                redirectAttributes.addFlashAttribute(
                        "mensaje",
                        (newId > 0) ? "Libro creado con ID: " + newId : "No se pudo crear el libro."
                );

            } else if ("actualizar".equalsIgnoreCase(accionFinal)) {
                if (id == null) {
                    throw new IllegalArgumentException("Falta el id del libro.");
                }
                Libro l = buildLibro(id, titulo, autor, descripcion, stock, valor, idCategoria, true);
                boolean ok = libroRepository.update(l);
                redirectAttributes.addFlashAttribute("mensaje", ok ? "Libro actualizado." : "No se pudo actualizar el libro.");

            } else if ("eliminar".equalsIgnoreCase(accionFinal)) {
                if (id == null) {
                    throw new IllegalArgumentException("Falta el id del libro.");
                }
                boolean ok = libroRepository.delete(id);
                redirectAttributes.addFlashAttribute("mensaje", ok ? "Libro eliminado." : "No se pudo eliminar el libro.");
            }

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar libro: " + ex.getMessage());
        }

        return "redirect:/libros?accion=listar";
    }

    /**
     * Construye un objeto {@link Libro} a partir de los parámetros del formulario.
     *
     * <p>
     * Existe para centralizar validaciones y normalización:
     * </p>
     *
     * <ul>
     *   <li>Stock no puede ser negativo (se fuerza a 0).</li>
     *   <li>Disponible se calcula como {@code stock > 0}.</li>
     *   <li>Valor no puede ser negativo (se fuerza a 0.00).</li>
     *   <li>Categoría puede ser nula si no se selecciona.</li>
     * </ul>
     */
    private Libro buildLibro(Integer id, String titulo, String autor, String descripcion, Integer stock, String valorStr, String idCategoriaStr, boolean includeId) {
        Libro l = new Libro();

        if (includeId && id != null) {
            l.setIdLibro(id);
        }

        l.setTitulo(titulo);
        l.setAutor(autor);
        l.setDescripcion(descripcion);

        int stockFinal = (stock == null) ? 0 : Math.max(stock, 0);
        l.setStock(stockFinal);
        l.setDisponible(stockFinal > 0);

        // El valor representa el precio unitario del libro en el catálogo.
        // Se recibe como String porque viene del formulario HTML.
        if (valorStr == null || valorStr.trim().isEmpty()) {
            l.setValor(BigDecimal.ZERO);
        } else {
            BigDecimal v = new BigDecimal(valorStr.trim());
            if (v.compareTo(BigDecimal.ZERO) < 0) {
                v = BigDecimal.ZERO;
            }
            l.setValor(v);
        }

        if (idCategoriaStr == null || idCategoriaStr.trim().isEmpty()) {
            l.setIdCategoria(null);
        } else {
            l.setIdCategoria(Integer.valueOf(idCategoriaStr));
        }

        return l;
    }
}
