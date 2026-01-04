package controller;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Muestra el panel principal (dashboard) después del login.
 *
 * <p>
 * Esta pantalla está protegida por el filtro {@code AuthFilter}, por lo que solo se puede
 * acceder si existe una sesión con el atributo {@code usuarioLogueado}.
 * </p>
 */
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/jsp/dashboard.jsp").forward(request, response);
    }
}
