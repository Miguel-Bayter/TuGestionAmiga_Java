package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Usuario;
import service.UsuarioService;

/**
 * Controla el inicio de sesión.
 *
 * <p>
 * Funciona así:
 * </p>
 *
 * <ol>
 *   <li>GET: muestra el formulario de login (JSP).</li>
 *   <li>POST: valida correo y contraseña contra la base de datos.</li>
 *   <li>Si es correcto, crea la sesión y redirige al dashboard.</li>
 * </ol>
 *
 * <p>
 * Nota: cuando el usuario se registra, se deja un mensaje en sesión (por ejemplo
 * "Registro exitoso...") para mostrarlo en la pantalla de login.
 * </p>
 */
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");

        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            request.setAttribute("error", "Debes ingresar correo y contraseña.");
            request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
            return;
        }

        try {
            UsuarioService usuarioService = new UsuarioService(getServletContext());
            Usuario usuario = usuarioService.autenticar(correo, contrasena);

            if (usuario == null) {
                request.setAttribute("error", "Credenciales incorrectas.");
                request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
                return;
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("usuarioLogueado", usuario);

            response.sendRedirect(request.getContextPath() + "/dashboard");

        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);

        } catch (Exception ex) {
            request.setAttribute("error", "Ocurrió un error al intentar iniciar sesión: " + ex.getMessage());
            request.getRequestDispatcher("/jsp/login.jsp").forward(request, response);
        }
    }
}
