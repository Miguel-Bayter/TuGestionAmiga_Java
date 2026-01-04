package filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Filtro de autenticación.
 *
 * <p>
 * La idea es que el usuario primero inicie sesión. Si no hay sesión, este filtro evita
 * que se acceda al resto de páginas.
 * </p>
 *
 * <p>
 * Se dejan abiertas (sin sesión) solo las páginas necesarias para entrar al sistema:
 * login, registro y recursos estáticos.
 * </p>
 */
public class AuthFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String path = req.getRequestURI();

        // Se permiten recursos estáticos y pantallas públicas (login/registro).
        if (path.endsWith("/login")
                || path.endsWith("/register")
                || path.endsWith("/index.jsp")
                || path.contains("/css/")
                || path.contains("/js/")
                || path.contains("/jsp/login.jsp")
                || path.contains("/jsp/register.jsp")) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        Object usuario = (session != null) ? session.getAttribute("usuarioLogueado") : null;

        if (usuario == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
