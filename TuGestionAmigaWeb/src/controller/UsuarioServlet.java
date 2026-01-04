package controller;

import dao.UsuarioDAO;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Usuario;

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

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "listar";
        }

        try {
            UsuarioDAO dao = new UsuarioDAO(getServletContext());

            if ("editar".equalsIgnoreCase(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Usuario usuarioEdit = dao.findById(id);
                request.setAttribute("usuarioEdit", usuarioEdit);
            }

            List<Usuario> usuarios = dao.findAll();
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

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "crear";
        }

        try {
            UsuarioDAO dao = new UsuarioDAO(getServletContext());

            if ("crear".equalsIgnoreCase(accion)) {
                Usuario u = buildUsuarioFromRequest(request, false);
                int id = dao.create(u);
                request.getSession().setAttribute("mensaje", (id > 0) ? "Usuario creado con ID: " + id : "No se pudo crear el usuario.");

            } else if ("actualizar".equalsIgnoreCase(accion)) {
                Usuario u = buildUsuarioFromRequest(request, true);
                boolean ok = dao.update(u);
                request.getSession().setAttribute("mensaje", ok ? "Usuario actualizado." : "No se pudo actualizar el usuario.");

            } else if ("eliminar".equalsIgnoreCase(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                boolean ok = dao.delete(id);
                request.getSession().setAttribute("mensaje", ok ? "Usuario eliminado." : "No se pudo eliminar el usuario.");
            }

            response.sendRedirect(request.getContextPath() + "/usuarios?accion=listar");

        } catch (Exception ex) {
            request.getSession().setAttribute("error", "Error al procesar usuario: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/usuarios?accion=listar");
        }
    }

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
