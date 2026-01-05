package com.tugestionamiga.spring.controller;

import com.tugestionamiga.spring.model.Usuario;
import com.tugestionamiga.spring.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controlador del CRUD de usuarios.
 *
 * <p>
 * Esta ruta está pensada para administración (rol ADMIN). Además de las operaciones CRUD,
 * se incluye una validación para impedir que un usuario elimine su propia cuenta mientras
 * tiene la sesión iniciada.
 * </p>
 */
@Controller
public class UsuariosController {

    private final UsuarioRepository usuarioRepository;

    public UsuariosController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/usuarios")
    public String usuarios(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "id", required = false) Integer id,
            Authentication authentication,
            Model model
    ) {
        String accionFinal = (accion == null || accion.trim().isEmpty()) ? "listar" : accion.trim();

        if (authentication != null && authentication.getName() != null) {
            usuarioRepository.findIdByCorreo(authentication.getName())
                    .ifPresent(idUsuarioSesion -> model.addAttribute("idUsuarioSesion", idUsuarioSesion));
        }

        if ("editar".equalsIgnoreCase(accionFinal) && id != null) {
            Optional<Usuario> usuarioEdit = usuarioRepository.findById(id);
            usuarioEdit.ifPresent(u -> model.addAttribute("usuarioEdit", u));
        }

        List<Usuario> usuarios = usuarioRepository.findAll();
        model.addAttribute("usuarios", usuarios);

        return "usuarios";
    }

    @PostMapping("/usuarios")
    public String usuariosPost(
            @RequestParam(name = "accion", required = false) String accion,
            @RequestParam(name = "id", required = false) Integer id,
            @RequestParam(name = "nombre", required = false) String nombre,
            @RequestParam(name = "correo", required = false) String correo,
            @RequestParam(name = "contrasena", required = false) String contrasena,
            @RequestParam(name = "idRol", required = false) Integer idRol,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        String accionFinal = (accion == null || accion.trim().isEmpty()) ? "crear" : accion.trim();

        try {
            if ("crear".equalsIgnoreCase(accionFinal)) {
                Usuario u = new Usuario();
                u.setNombre(nombre);
                u.setCorreo(correo);
                u.setContrasena(contrasena);
                u.setIdRol(idRol);

                int newId = usuarioRepository.create(u);
                redirectAttributes.addFlashAttribute(
                        "mensaje",
                        (newId > 0) ? "Usuario creado con ID: " + newId : "No se pudo crear el usuario."
                );

            } else if ("actualizar".equalsIgnoreCase(accionFinal)) {
                if (id == null) {
                    throw new IllegalArgumentException("Falta el id del usuario.");
                }
                Usuario u = new Usuario();
                u.setIdUsuario(id);
                u.setNombre(nombre);
                u.setCorreo(correo);
                u.setContrasena(contrasena);
                u.setIdRol(idRol);

                boolean ok = usuarioRepository.update(u);
                redirectAttributes.addFlashAttribute("mensaje", ok ? "Usuario actualizado." : "No se pudo actualizar el usuario.");

            } else if ("eliminar".equalsIgnoreCase(accionFinal)) {
                if (id == null) {
                    throw new IllegalArgumentException("Falta el id del usuario.");
                }

                if (authentication != null && authentication.getName() != null) {
                    Optional<Integer> idSesion = usuarioRepository.findIdByCorreo(authentication.getName());
                    if (idSesion.isPresent() && idSesion.get().equals(id)) {
                        redirectAttributes.addFlashAttribute("error", "No puedes eliminar tu propio usuario mientras tienes sesión iniciada.");
                        return "redirect:/usuarios?accion=listar";
                    }
                }

                boolean ok = usuarioRepository.delete(id);
                redirectAttributes.addFlashAttribute("mensaje", ok ? "Usuario eliminado." : "No se pudo eliminar el usuario.");
            }

        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Error al procesar usuario: " + ex.getMessage());
        }

        return "redirect:/usuarios?accion=listar";
    }
}
