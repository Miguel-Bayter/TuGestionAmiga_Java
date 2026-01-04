package controller;

import dao.LibroDAO;
import java.io.IOException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Libro;

/**
 * Servlet para administrar libros (CRUD).
 *
 * <p>
 * Se decidió manejar las acciones con el parámetro {@code accion} para no crear muchos servlets.
 * La JSP usada es {@code /jsp/listarLibros.jsp}.
 * </p>
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

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "listar";
        }

        try {
            LibroDAO dao = new LibroDAO(getServletContext());

            if ("editar".equalsIgnoreCase(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                Libro libroEdit = dao.findById(id);
                request.setAttribute("libroEdit", libroEdit);
            }

            List<Libro> libros = dao.findAll();
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

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "crear";
        }

        try {
            LibroDAO dao = new LibroDAO(getServletContext());

            if ("crear".equalsIgnoreCase(accion)) {
                Libro l = buildLibroFromRequest(request, false);
                int id = dao.create(l);
                request.getSession().setAttribute("mensaje", (id > 0) ? "Libro creado con ID: " + id : "No se pudo crear el libro.");

            } else if ("actualizar".equalsIgnoreCase(accion)) {
                Libro l = buildLibroFromRequest(request, true);
                boolean ok = dao.update(l);
                request.getSession().setAttribute("mensaje", ok ? "Libro actualizado." : "No se pudo actualizar el libro.");

            } else if ("eliminar".equalsIgnoreCase(accion)) {
                int id = Integer.parseInt(request.getParameter("id"));
                boolean ok = dao.delete(id);
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
