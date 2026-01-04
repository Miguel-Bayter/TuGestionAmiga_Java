package util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.ServletContext;

/**
 * Centraliza la creación de conexiones JDBC a MySQL para el módulo web.
 *
 * <p>
 * En una aplicación web no es buena idea leer archivos con rutas relativas (como en consola),
 * porque el directorio de trabajo depende de Tomcat. Por eso esta clase lee el archivo
 * {@code /WEB-INF/db.properties} usando el {@link ServletContext}.
 * </p>
 *
 * <p>
 * Si el archivo no existe o no se puede leer, se usan valores por defecto.
 * </p>
 */
public class ConexionBD {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/tugestionamiga_db?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private ConexionBD() {
    }

    /**
     * Obtiene una conexión JDBC usando propiedades guardadas en el contexto de la aplicación.
     *
     * @param context contexto de la aplicación (ServletContext)
     * @return conexión activa a MySQL
     * @throws SQLException si falla la conexión
     */
    public static Connection getConnection(ServletContext context) throws SQLException {
        Properties props = (Properties) context.getAttribute("db.properties");
        if (props == null) {
            props = loadProperties(context);
            context.setAttribute("db.properties", props);
        }

        String url = props.getProperty("db.url", DEFAULT_URL);
        String user = props.getProperty("db.user", DEFAULT_USER);
        String password = props.getProperty("db.password", DEFAULT_PASSWORD);

        // Se fuerza la carga del driver. En algunos casos, si el conector no está bien en el classpath,
        // puede aparecer el error "No suitable driver".
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException ex2) {
                throw new SQLException("No se encontró el controlador JDBC de MySQL en el proyecto web. "
                        + "Verifica que mysql-connector-j esté en WEB-INF/lib o agregado a las bibliotecas del proyecto.", ex2);
            }
        }

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Carga el archivo {@code /WEB-INF/db.properties} si existe.
     *
     * @param context contexto de la aplicación
     * @return propiedades leídas o un objeto vacío si no se encontró
     */
    public static Properties loadProperties(ServletContext context) {
        Properties props = new Properties();

        try (InputStream is = context.getResourceAsStream("/WEB-INF/db.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException ex) {
            // Si no se puede leer el archivo, se usarán valores por defecto.
        }

        return props;
    }
}
