package controller;

import dao.LibroDAO;
import dao.PrestamoDAO;
import dao.UsuarioDAO;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.Libro;
import model.Prestamo;
import model.Usuario;

/**
 * Servlet para administrar préstamos.
 *
 * <p>
 * Incluye las operaciones principales del negocio:
 * </p>
 *
 * <ul>
 *   <li>Registrar préstamo: crea un préstamo y cambia disponibilidad del libro.</li>
 *   <li>Registrar devolución: marca el préstamo como DEVUELTO y libera el libro.</li>
 * </ul>
 */
public class PrestamoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            UsuarioDAO usuarioDAO = new UsuarioDAO(getServletContext());
            LibroDAO libroDAO = new LibroDAO(getServletContext());
            PrestamoDAO prestamoDAO = new PrestamoDAO(getServletContext());

            List<Usuario> usuarios = usuarioDAO.findAll();
            List<Libro> libros = libroDAO.findAll();
            List<Prestamo> prestamos = prestamoDAO.findAll();

            request.setAttribute("usuarios", usuarios);
            request.setAttribute("libros", libros);
            request.setAttribute("prestamos", prestamos);

            request.getRequestDispatcher("/jsp/prestamos.jsp").forward(request, response);

        } catch (Exception ex) {
            request.setAttribute("error", "Error al cargar préstamos: " + ex.getMessage());
            request.getRequestDispatcher("/jsp/prestamos.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null || accion.trim().isEmpty()) {
            accion = "registrarPrestamo";
        }

        try {
            PrestamoDAO dao = new PrestamoDAO(getServletContext());

            if ("registrarPrestamo".equalsIgnoreCase(accion)) {
                int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                int idLibro = Integer.parseInt(request.getParameter("idLibro"));
                LocalDate fechaPrestamo = LocalDate.parse(request.getParameter("fechaPrestamo"));
                LocalDate fechaDevolucion = LocalDate.parse(request.getParameter("fechaDevolucion"));

                int idPrestamo = dao.registrarPrestamo(idUsuario, idLibro, fechaPrestamo, fechaDevolucion);

                if (idPrestamo > 0) {
                    request.getSession().setAttribute("mensaje", "Préstamo registrado con ID: " + idPrestamo);
                } else {
                    request.getSession().setAttribute("error", "No se pudo registrar el préstamo. Verifica que el libro exista y esté disponible.");
                }

            } else if ("registrarDevolucion".equalsIgnoreCase(accion)) {
                int idPrestamo = Integer.parseInt(request.getParameter("idPrestamo"));
                boolean ok = dao.registrarDevolucion(idPrestamo);

                request.getSession().setAttribute("mensaje", ok ? "Devolución registrada." : "No se pudo registrar la devolución.");

            } else if ("eliminar".equalsIgnoreCase(accion)) {
                int idPrestamo = Integer.parseInt(request.getParameter("id"));
                boolean ok = dao.delete(idPrestamo);
                request.getSession().setAttribute("mensaje", ok ? "Préstamo eliminado." : "No se pudo eliminar el préstamo.");
            }

            response.sendRedirect(request.getContextPath() + "/prestamos");

        } catch (Exception ex) {
            request.getSession().setAttribute("error", "Error al procesar préstamo: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/prestamos");
        }
    }
}
