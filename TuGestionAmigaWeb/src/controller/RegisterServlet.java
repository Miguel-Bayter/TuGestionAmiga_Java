package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import service.UsuarioService;

/**
 * Controla el registro de usuarios desde la aplicación web.
 *
 * <p>
 * La idea de este servlet es permitir que una persona cree su cuenta y luego
 * pueda iniciar sesión con el mismo correo y contraseña.
 * </p>
 *
 * <p>
 * Flujo:
 * </p>
 *
 * <ol>
 *   <li>GET: muestra el formulario de registro.</li>
 *   <li>POST: valida datos, verifica que el correo no esté repetido y crea el usuario.</li>
 *   <li>Si todo sale bien, redirige a login con un mensaje de confirmación.</li>
 * </ol>
 */
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Muestra la vista del formulario de registro.
        request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Se leen los datos del formulario.
        String nombre = request.getParameter("nombre");
        String correo = request.getParameter("correo");
        String contrasena = request.getParameter("contrasena");
        String contrasena2 = request.getParameter("contrasena2");

        try {
            UsuarioService usuarioService = new UsuarioService(getServletContext());
            usuarioService.registrarUsuarioPublico(nombre, correo, contrasena, contrasena2);

            // Se usa sesión para mostrar un mensaje en la pantalla de login.
            HttpSession session = request.getSession(true);
            session.setAttribute("mensaje", "Registro exitoso. Ya puedes iniciar sesión.");
            response.sendRedirect(request.getContextPath() + "/login");

        } catch (IllegalArgumentException ex) {
            request.setAttribute("error", ex.getMessage());
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);

        } catch (Exception ex) {
            request.setAttribute("error", "Ocurrió un error al registrarse: " + ex.getMessage());
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
        }
    }
}
