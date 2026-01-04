package controller;

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
import service.LibroService;
import service.PrestamoService;
import service.UsuarioService;

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
 *
 * <p>
 * Permisos:
 * </p>
 *
 * <ul>
 *   <li><strong>ADMIN</strong>: ve todos los préstamos, puede registrar devoluciones y eliminar.</li>
 *   <li><strong>USUARIO</strong>: ve solo sus préstamos y registra préstamos para sí mismo.</li>
 * </ul>
 */
public class PrestamoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            Usuario usuarioSesion = null;
            if (request.getSession(false) != null) {
                usuarioSesion = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");
            }

            if (usuarioSesion == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            // Se separa la pantalla en dos modos:
            // - ADMIN: puede ver todos los préstamos y operar devoluciones/eliminación.
            // - USUARIO: ve solo sus préstamos y registra préstamos para sí mismo.
            boolean esAdmin = (usuarioSesion != null && usuarioSesion.esAdministrador());

            UsuarioService usuarioService = new UsuarioService(getServletContext());
            LibroService libroService = new LibroService(getServletContext());
            PrestamoService prestamoService = new PrestamoService(getServletContext());

            List<Libro> libros = libroService.listarLibros();

            List<Usuario> usuarios = null;
            List<Prestamo> prestamos;

            if (esAdmin) {
                usuarios = usuarioService.listarUsuarios();
                prestamos = prestamoService.listarPrestamos();
            } else {
                // En modo usuario se limita el listado al usuario logueado.
                prestamos = prestamoService.listarPrestamosPorUsuario(usuarioSesion.getIdUsuario());
            }

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
            Usuario usuarioSesion = null;
            if (request.getSession(false) != null) {
                usuarioSesion = (Usuario) request.getSession(false).getAttribute("usuarioLogueado");
            }

            if (usuarioSesion == null) {
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }

            boolean esAdmin = (usuarioSesion != null && usuarioSesion.esAdministrador());
            PrestamoService prestamoService = new PrestamoService(getServletContext());

            if ("registrarPrestamo".equalsIgnoreCase(accion)) {
                int idUsuario;
                if (esAdmin) {
                    idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
                } else {
                    // En modo usuario se fuerza a que el préstamo sea del usuario logueado.
                    // Aunque el formulario sea manipulado, el servidor usa el dato de sesión.
                    idUsuario = usuarioSesion.getIdUsuario();
                }
                int idLibro = Integer.parseInt(request.getParameter("idLibro"));
                LocalDate fechaPrestamo = LocalDate.parse(request.getParameter("fechaPrestamo"));
                LocalDate fechaDevolucion = LocalDate.parse(request.getParameter("fechaDevolucion"));

                int idPrestamo = prestamoService.registrarPrestamo(idUsuario, idLibro, fechaPrestamo, fechaDevolucion);

                if (idPrestamo > 0) {
                    request.getSession().setAttribute("mensaje", "Préstamo registrado con ID: " + idPrestamo);
                } else {
                    request.getSession().setAttribute("error", "No se pudo registrar el préstamo. Verifica que el libro exista y esté disponible.");
                }

            } else if ("registrarDevolucion".equalsIgnoreCase(accion)) {
                if (!esAdmin) {
                    request.getSession().setAttribute("error", "Acceso restringido: solo un administrador puede registrar devoluciones.");
                    response.sendRedirect(request.getContextPath() + "/prestamos");
                    return;
                }
                int idPrestamo = Integer.parseInt(request.getParameter("idPrestamo"));
                boolean ok = prestamoService.registrarDevolucion(idPrestamo);

                request.getSession().setAttribute("mensaje", ok ? "Devolución registrada." : "No se pudo registrar la devolución.");

            } else if ("eliminar".equalsIgnoreCase(accion)) {
                if (!esAdmin) {
                    request.getSession().setAttribute("error", "Acceso restringido: solo un administrador puede eliminar préstamos.");
                    response.sendRedirect(request.getContextPath() + "/prestamos");
                    return;
                }
                int idPrestamo = Integer.parseInt(request.getParameter("id"));
                boolean ok = prestamoService.eliminarPrestamo(idPrestamo);
                request.getSession().setAttribute("mensaje", ok ? "Préstamo eliminado." : "No se pudo eliminar el préstamo.");
            }

            response.sendRedirect(request.getContextPath() + "/prestamos");

        } catch (Exception ex) {
            request.getSession().setAttribute("error", "Error al procesar préstamo: " + ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/prestamos");
        }
    }
}
