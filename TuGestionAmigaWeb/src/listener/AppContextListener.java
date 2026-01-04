package listener;

import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import util.ConexionBD;

/**
 * Listener que se ejecuta cuando la aplicación web inicia y finaliza.
 *
 * <p>
 * Se usa para cargar una sola vez el archivo {@code /WEB-INF/db.properties} y dejarlo
 * disponible para todos los Servlets y DAOs a través del {@link ServletContext}.
 * </p>
 */
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        Properties props = ConexionBD.loadProperties(context);
        context.setAttribute("db.properties", props);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // No se requiere limpieza especial porque las conexiones se abren/cerran por operación.
    }
}
