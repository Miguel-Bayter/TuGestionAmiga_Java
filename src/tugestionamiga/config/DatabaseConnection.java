package tugestionamiga.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Encapsula la obtención de conexiones JDBC a la base de datos MySQL.
 *
 * La clase intenta leer credenciales desde un archivo {@code db.properties}
 * ubicado en la raíz del proyecto. Si no existe, usa valores por defecto.
 */
public class DatabaseConnection {

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/tugestionamiga_db?useSSL=false&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private DatabaseConnection() {
    }

    /**
     * Crea y devuelve una conexión JDBC.
     *
     * @return conexión activa a MySQL
     * @throws SQLException si ocurre un error de conexión
     */
    public static Connection getConnection() throws SQLException {
        Properties props = loadProperties();

        String url = props.getProperty("db.url", DEFAULT_URL);
        String user = props.getProperty("db.user", DEFAULT_USER);
        String password = props.getProperty("db.password", DEFAULT_PASSWORD);

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Intenta cargar propiedades desde el archivo {@code db.properties}.
     * Si el archivo no existe o no se puede leer, se devuelve un objeto vacío.
     */
    private static Properties loadProperties() {
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
        } catch (IOException ex) {
            // Si no existe el archivo, se usan los valores por defecto.
        }

        return props;
    }
}
