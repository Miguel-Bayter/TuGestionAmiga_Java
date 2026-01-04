package controller;

import dao.UsuarioDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import model.Usuario;

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

        // Validación básica: no permitir campos vacíos.
        if (nombre == null || nombre.trim().isEmpty()
                || correo == null || correo.trim().isEmpty()
                || contrasena == null || contrasena.trim().isEmpty()
                || contrasena2 == null || contrasena2.trim().isEmpty()) {
            request.setAttribute("error", "Debes completar todos los campos.");
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
            return;
        }

        // Validación básica: que ambas contraseñas coincidan.
        if (!contrasena.trim().equals(contrasena2.trim())) {
            request.setAttribute("error", "Las contraseñas no coinciden.");
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
            return;
        }

        try {
            UsuarioDAO dao = new UsuarioDAO(getServletContext());

            // Se valida que el correo sea único.
            Usuario existente = dao.findByCorreo(correo.trim());
            if (existente != null) {
                request.setAttribute("error", "Ya existe un usuario registrado con ese correo.");
                request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
                return;
            }

            // Se construye el objeto Usuario para insertarlo en BD.
            Usuario u = new Usuario();
            u.setNombre(nombre.trim());
            u.setCorreo(correo.trim());
            u.setContrasena(contrasena.trim());
            u.setIdRol(null);

            dao.create(u);

            // Se usa sesión para mostrar un mensaje en la pantalla de login.
            HttpSession session = request.getSession(true);
            session.setAttribute("mensaje", "Registro exitoso. Ya puedes iniciar sesión.");
            response.sendRedirect(request.getContextPath() + "/login");

        } catch (Exception ex) {
            request.setAttribute("error", "Ocurrió un error al registrarse: " + ex.getMessage());
            request.getRequestDispatcher("/jsp/register.jsp").forward(request, response);
        }
    }
}
