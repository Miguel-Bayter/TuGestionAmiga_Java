package com.tugestionamiga.spring.controller;

import com.tugestionamiga.spring.model.Libro;
import com.tugestionamiga.spring.repository.CategoriaRepository;
import com.tugestionamiga.spring.repository.LibroRepository;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LibrosController {

    private final LibroRepository libroRepository;
    private final CategoriaRepository categoriaRepository;

    public LibrosController(LibroRepository libroRepository, CategoriaRepository categoriaRepository) {
        this.libroRepository = libroRepository;
        this.categoriaRepository = categoriaRepository;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

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

    @PostMapping("/libros")
    public String librosPost(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "id", required = false) Integer id,
            @RequestParam(name = "titulo", required = false) String titulo,
            @RequestParam(name = "autor", required = false) String autor,
            @RequestParam(name = "descripcion", required = false) String descripcion,
            @RequestParam(name = "disponible", required = false) String disponible,
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
                Libro l = buildLibro(id, titulo, autor, descripcion, disponible, idCategoria, false);
                int newId = libroRepository.create(l);
                redirectAttributes.addFlashAttribute(
                        "mensaje",
                        (newId > 0) ? "Libro creado con ID: " + newId : "No se pudo crear el libro."
                );

            } else if ("actualizar".equalsIgnoreCase(accionFinal)) {
                if (id == null) {
                    throw new IllegalArgumentException("Falta el id del libro.");
                }
                Libro l = buildLibro(id, titulo, autor, descripcion, disponible, idCategoria, true);
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

    private Libro buildLibro(Integer id, String titulo, String autor, String descripcion, String disponible, String idCategoriaStr, boolean includeId) {
        Libro l = new Libro();

        if (includeId && id != null) {
            l.setIdLibro(id);
        }

        l.setTitulo(titulo);
        l.setAutor(autor);
        l.setDescripcion(descripcion);

        boolean disp = "1".equals(disponible) || "true".equalsIgnoreCase(disponible) || "on".equalsIgnoreCase(disponible);
        l.setDisponible(disp);

        if (idCategoriaStr == null || idCategoriaStr.trim().isEmpty()) {
            l.setIdCategoria(null);
        } else {
            l.setIdCategoria(Integer.valueOf(idCategoriaStr));
        }

        return l;
    }
}
