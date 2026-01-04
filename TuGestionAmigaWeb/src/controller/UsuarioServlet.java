package controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Usuario;
import service.UsuarioService;

/**
 * Servlet para administrar usuarios (CRUD).
 *
 * <p>
 * Se decidió manejar las acciones con el parámetro {@code accion} para no crear muchos servlets:
 * </p>
 *
 * <ul>
 *   <li>GET /usuarios?accion=listar</li>
 *   <li>GET /usuarios?accion=editar&id=...</li>
 *   <li>POST /usuarios (crear, actualizar, eliminar)</li>
 * </ul>
 */
public class UsuarioServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuarioSesion = null;
        if (request.getSession(false) != null) {
            usuarioSesion = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");
        }
        // Este servlet se usa como administración de usuarios, por eso se limita a rol administrador.
        // La validación se hace aquí (servidor) además de ocultar enlaces en la interfaz.
        if (usuarioSesion == null || !usuarioSesion.esAdministrador()) {
            request.getSession().setAttribute("error", "Acceso restringido: esta sección es solo para administradores.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "listar";
        }

        try {
            UsuarioService usuarioService = new UsuarioService(getServletContext());

            if ("editar".equalsIgnoreCase(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Usuario usuarioEdit = usuarioService.obtenerPorId(id);
                request.setAttribute("usuarioEdit", usuarioEdit);
            }

            List<Usuario> usuarios = usuarioService.listarUsuarios();
            request.setAttribute("usuarios", usuarios);

            request.getRequestDispatcher("/jsp/usuarios.jsp").forward(request, response);

        } catch (Exception ex) {
            request.setAttribute("error", "Error al cargar usuarios: " + ex.getMessage());
            request.getRequestDispatcher("/jsp/usuarios.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuarioSesion = null;
        if (request.getSession(false) != null) {
            usuarioSesion = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");
        }
        // Al igual que en doGet, se valida el rol en el servidor.
        // Esto evita que un usuario normal pueda ejecutar un POST directo a /usuarios.
        if (usuarioSesion == null || !usuarioSesion.esAdministrador()) {
            request.getSession().setAttribute("error", "Acceso restringido: esta sección es solo para administradores.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }

        // En POST se manejan las acciones que modifican datos (crear/actualizar/eliminar).
        // El flujo termina redirigiendo a listar para evitar reenvíos al refrescar la página.
        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "crear";
        }

        try {
            UsuarioService usuarioService = new UsuarioService(getServletContext());

            if ("crear".equalsIgnoreCase(accion)) {
                Usuario u = buildUsuarioFromRequest(request, false);
                int id = usuarioService.crearUsuario(u);
                request.getSession().setAttribute("mensaje", (id > 0) ? "Usuario creado con ID: " + id : "No se pudo crear el usuario.");

            } else if ("actualizar".equalsIgnoreCase(accion)) {
                Usuario u = buildUsuarioFromRequest(request, true);
                boolean ok = usuarioService.actualizarUsuario(u);
                request.getSession().setAttribute("mensaje", ok ? "Usuario actualizado." : "No se pudo actualizar el usuario.");

            } else if ("eliminar".equalsIgnoreCase(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));

                // Regla simple de seguridad: evitar que un usuario se elimine a sí mismo.
                // Si se permitiera, la sesión quedaría en un estado confuso (usuario logueado borrado).
                if (usuarioSesion != null && usuarioSesion.getIdUsuario() == id) {
                    request.getSession().setAttribute("error", "No puedes eliminar tu propio usuario mientras tienes sesión iniciada.");
                    response.sendRedirect(request.getContextPath() + "/usuarios?accion=listar");
                    return;
                }

                boolean ok = usuarioService.eliminarUsuario(id);
                request.getSession().setAttribute("mensaje", ok ? "Usuario eliminado." : "No se pudo eliminar el usuario.");
            }

            response.sendRedirect(request.getContextPath() + "/usuarios?accion=listar");

        } catch (Exception ex) {
            request.getSession().setAttribute("error", "Error al procesar usuario: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/usuarios?accion=listar");
        }
    }

    /**
     * Construye un objeto {@link Usuario} a partir de parámetros HTTP.
     *
     * <p>
     * Se usa para evitar repetir el mismo mapeo en crear y actualizar.
     * El campo {@code idRol} es opcional: si viene vacío se guarda como {@code null}.
     * </p>
     */
    private Usuario buildUsuarioFromRequest(HttpServletRequest request, boolean includeId) {
        Usuario u = new Usuario();

        if (includeId) {
            u.setIdUsuario(Integer.parseInt(request.getParameter("id")));
        }

        u.setNombre(request.getParameter("nombre"));
        u.setCorreo(request.getParameter("correo"));
        u.setContrasena(request.getParameter("contrasena"));

        String idRolStr = request.getParameter("idRol");
        if (idRolStr == null || idRolStr.trim().isEmpty()) {
            u.setIdRol(null);
        } else {
            u.setIdRol(Integer.parseInt(idRolStr));
        }

        return u;
    }
}
