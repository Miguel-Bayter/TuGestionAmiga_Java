package controller;

import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Libro;
import model.Usuario;
import service.LibroService;

/**
 * Servlet para administrar libros (CRUD).
 *
 * <p>
 * Se decidió manejar las acciones con el parámetro {@code accion} para no crear muchos servlets.
 * La JSP usada es {@code /jsp/listarLibros.jsp}.
 * </p>
 *
 * <p>
 * Permisos:
 * </p>
 *
 * <ul>
 *   <li><strong>ADMIN</strong>: puede crear, editar, actualizar y eliminar.</li>
 *   <li><strong>USUARIO</strong>: solo puede listar (lectura).</li>
 * </ul>
 *
 * <ul>
 *   <li>GET /libros?accion=listar</li>
 *   <li>GET /libros?accion=editar&id=...</li>
 *   <li>POST /libros (crear, actualizar, eliminar)</li>
 * </ul>
 */
public class LibroServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuarioSesion = null;
        if (request.getSession(false) != null) {
            usuarioSesion = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");
        }

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "listar";
        }

        try {
            LibroService libroService = new LibroService(getServletContext());

            if ("editar".equalsIgnoreCase(accion)) {
                // Solo un administrador puede entrar a modo edición.
                // Para el usuario normal se deja el listado como lectura.
                if (usuarioSesion == null || !usuarioSesion.esAdministrador()) {
                    request.getSession().setAttribute("error", "Acceso restringido: solo un administrador puede editar libros.");
                    response.sendRedirect(request.getContextPath() + "/libros?accion=listar");
                    return;
                }
                int id = Integer.parseInt(request.getParameter("id"));
                Libro libroEdit = libroService.obtenerPorId(id);
                request.setAttribute("libroEdit", libroEdit);
            }

            List<Libro> libros = libroService.listarLibros();
            request.setAttribute("libros", libros);

            request.getRequestDispatcher("/jsp/listarLibros.jsp").forward(request, response);

        } catch (Exception ex) {
            request.setAttribute("error", "Error al cargar libros: " + ex.getMessage());
            request.getRequestDispatcher("/jsp/listarLibros.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Usuario usuarioSesion = null;
        if (request.getSession(false) != null) {
            usuarioSesion = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");
        }
        if (usuarioSesion == null || !usuarioSesion.esAdministrador()) {
            // El CRUD de libros queda reservado para administrador.
            request.getSession().setAttribute("error", "Acceso restringido: solo un administrador puede modificar libros.");
            response.sendRedirect(request.getContextPath() + "/libros?accion=listar");
            return;
        }

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "crear";
        }

        try {
            LibroService libroService = new LibroService(getServletContext());

            if ("crear".equalsIgnoreCase(accion)) {
                Libro l = buildLibroFromRequest(request, false);
                int id = libroService.crearLibro(l);
                request.getSession().setAttribute("mensaje", (id > 0) ? "Libro creado con ID: " + id : "No se pudo crear el libro.");

            } else if ("actualizar".equalsIgnoreCase(accion)) {
                Libro l = buildLibroFromRequest(request, true);
                boolean ok = libroService.actualizarLibro(l);
                request.getSession().setAttribute("mensaje", ok ? "Libro actualizado." : "No se pudo actualizar el libro.");

            } else if ("eliminar".equalsIgnoreCase(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                boolean ok = libroService.eliminarLibro(id);
                request.getSession().setAttribute("mensaje", ok ? "Libro eliminado." : "No se pudo eliminar el libro.");
            }

            response.sendRedirect(request.getContextPath() + "/libros?accion=listar");

        } catch (Exception ex) {
            request.getSession().setAttribute("error", "Error al procesar libro: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/libros?accion=listar");
        }
    }

    /**
     * Arma un objeto {@link model.Libro} usando los datos del formulario.
     *
     * <p>
     * Este método se usa tanto para crear como para actualizar. Por eso existe el parámetro
     * {@code includeId}.
     * </p>
     */
    private Libro buildLibroFromRequest(HttpServletRequest request, boolean includeId) {
        Libro l = new Libro();

        if (includeId) {
            l.setIdLibro(Integer.parseInt(request.getParameter("id")));
        }

        l.setTitulo(request.getParameter("titulo"));
        l.setAutor(request.getParameter("autor"));
        l.setDescripcion(request.getParameter("descripcion"));

        String disponibleStr = request.getParameter("disponible");
        boolean disponible = "1".equals(disponibleStr) || "true".equalsIgnoreCase(disponibleStr) || "on".equalsIgnoreCase(disponibleStr);
        l.setDisponible(disponible);

        String idCategoriaStr = request.getParameter("idCategoria");
        if (idCategoriaStr == null || idCategoriaStr.trim().isEmpty()) {
            l.setIdCategoria(null);
        } else {
            l.setIdCategoria(Integer.parseInt(idCategoriaStr));
        }

        return l;
    }
}
