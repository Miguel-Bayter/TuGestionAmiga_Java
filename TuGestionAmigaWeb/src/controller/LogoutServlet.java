package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Cierra la sesión del usuario.
 *
 * <p>
 * Se invalida la sesión para eliminar el atributo {@code usuarioLogueado} y cualquier
 * otro dato guardado. Luego se redirige al login.
 * </p>
 */
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }

        response.sendRedirect(request.getContextPath() + "/login");
    }
}
