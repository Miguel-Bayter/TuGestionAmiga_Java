package com.tugestionamiga.spring.controller;

import com.tugestionamiga.spring.model.Usuario;
import com.tugestionamiga.spring.repository.UsuarioRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
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

    /**
     * Renderiza la vista de usuarios.
     *
     * <p>
     * Esta pantalla soporta dos modos:
     * </p>
     *
     * <ul>
     *   <li>Listar: muestra todos los usuarios.</li>
     *   <li>Editar: carga un usuario específico para precargar el formulario.</li>
     * </ul>
     *
     * <p>
     * También calcula {@code idUsuarioSesion} para deshabilitar acciones sobre el propio usuario.
     * </p>
     */
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

    /**
     * Procesa acciones del CRUD de usuarios.
     *
     * <p>
     * Acciones principales:
     * </p>
     *
     * <ul>
     *   <li>{@code crear}: inserta usuario.</li>
     *   <li>{@code actualizar}: modifica credenciales/rol.</li>
     *   <li>{@code eliminar}: solo ADMIN puede eliminar y no se permite auto-eliminación con sesión activa.</li>
     * </ul>
     *
     * <p>
     * Si el usuario en sesión cambia su correo, se refresca el {@code Authentication} para que
     * Spring Security use el nuevo correo como {@code username} sin obligar a cerrar sesión.
     * </p>
     */
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

                boolean esMismaSesion = false;
                String correoSesion = null;
                if (authentication != null && authentication.getName() != null) {
                    correoSesion = authentication.getName();
                    Optional<Integer> idSesion = usuarioRepository.findIdByCorreo(correoSesion);
                    esMismaSesion = idSesion.isPresent() && idSesion.get().equals(id);
                }

                if (esMismaSesion) {
                    Usuario usuarioActual = usuarioRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario a actualizar."));

                    Integer idRolActual = usuarioActual.getIdRol();
                    Integer idRolFinal = (idRol == null) ? idRolActual : idRol;

                    if (idRolFinal == null || !Integer.valueOf(1).equals(idRolFinal)) {
                        redirectAttributes.addFlashAttribute(
                                "error",
                                "No puedes cambiar tu propio rol a USUARIO. Debes mantenerte como ADMIN."
                        );
                        return "redirect:/usuarios?accion=editar&id=" + id;
                    }

                    idRol = idRolFinal;
                } else {
                    if (idRol == null) {
                        idRol = 2;
                    }
                }

                if (idRol != null && !Integer.valueOf(1).equals(idRol) && !Integer.valueOf(2).equals(idRol)) {
                    throw new IllegalArgumentException("Rol inválido. Solo se permite ADMIN (1) o USUARIO (2). ");
                }

                String correoAnterior = null;
                if (esMismaSesion) {
                    correoAnterior = correoSesion;
                }

                Usuario u = new Usuario();
                u.setIdUsuario(id);
                u.setNombre(nombre);
                u.setCorreo(correo);
                u.setContrasena(contrasena);
                u.setIdRol(idRol);

                boolean ok = usuarioRepository.update(u);
                redirectAttributes.addFlashAttribute("mensaje", ok ? "Usuario actualizado." : "No se pudo actualizar el usuario.");

                if (ok && esMismaSesion && correoAnterior != null && correo != null && !correoAnterior.equalsIgnoreCase(correo)) {
                    List<GrantedAuthority> auths = authentication.getAuthorities().stream()
                            .map(a -> (GrantedAuthority) a)
                            .collect(Collectors.toList());

                    User principal = new User(correo, "", auths);
                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            principal,
                            authentication.getCredentials(),
                            authentication.getAuthorities()
                    );
                    newAuth.setDetails(authentication.getDetails());
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }

            } else if ("eliminar".equalsIgnoreCase(accionFinal)) {
                if (id == null) {
                    throw new IllegalArgumentException("Falta el id del usuario.");
                }

                if (authentication == null || authentication.getAuthorities() == null
                        || authentication.getAuthorities().stream().noneMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()))) {
                    redirectAttributes.addFlashAttribute("error", "Solo un ADMIN puede eliminar usuarios.");
                    return "redirect:/dashboard";
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
