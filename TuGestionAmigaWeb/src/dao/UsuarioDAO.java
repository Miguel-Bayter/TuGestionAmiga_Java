package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import model.Usuario;
import util.ConexionBD;

/**
 * DAO para la entidad {@link model.Usuario} en el módulo web.
 *
 * <p>
 * La diferencia principal con el módulo de consola es que aquí la conexión se obtiene usando
 * {@link util.ConexionBD} y el {@link ServletContext}, para leer la configuración desde
 * {@code /WEB-INF/db.properties}.
 * </p>
 */
public class UsuarioDAO {

    private final ServletContext context;

    /**
     * Crea el DAO.
     *
     * @param context contexto de la aplicación (necesario para abrir conexiones)
     */
    public UsuarioDAO(ServletContext context) {
        this.context = context;
    }

    /**
     * Inserta un usuario en la tabla {@code usuario}.
     */
    public int create(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, correo, `contraseña`, id_rol) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getCorreo());
            stmt.setString(3, usuario.getContrasena());

            if (usuario.getIdRol() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, usuario.getIdRol());
            }

            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        return -1;
    }

    /**
     * Busca un usuario por su ID.
     */
    public Usuario findById(int idUsuario) throws SQLException {
        String sql = "SELECT id_usuario, nombre, correo, `contraseña` AS contrasena, id_rol FROM usuario WHERE id_usuario = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUsuario(rs);
                }
            }
        }

        return null;
    }

    /**
     * Busca un usuario por correo.
     *
     * <p>
     * Se usa principalmente en el registro para validar que no exista ya un usuario
     * con el mismo correo.
     * </p>
     *
     * @param correo correo a buscar
     * @return el usuario si existe, o {@code null} si no se encontró
     * @throws SQLException si ocurre un error al consultar la base de datos
     */
    public Usuario findByCorreo(String correo) throws SQLException {
        String sql = "SELECT id_usuario, nombre, correo, `contraseña` AS contrasena, id_rol "
                + "FROM usuario WHERE correo = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUsuario(rs);
                }
            }
        }

        return null;
    }

    /**
     * Lista todos los usuarios.
     */
    public List<Usuario> findAll() throws SQLException {
        String sql = "SELECT id_usuario, nombre, correo, `contraseña` AS contrasena, id_rol FROM usuario ORDER BY id_usuario";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapUsuario(rs));
            }
        }

        return usuarios;
    }

    /**
     * Actualiza los datos de un usuario.
     */
    public boolean update(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nombre = ?, correo = ?, `contraseña` = ?, id_rol = ? WHERE id_usuario = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getCorreo());
            stmt.setString(3, usuario.getContrasena());

            if (usuario.getIdRol() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, usuario.getIdRol());
            }

            stmt.setInt(5, usuario.getIdUsuario());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un usuario por ID.
     */
    public boolean delete(int idUsuario) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Busca un usuario por correo y contraseña.
     *
     * <p>
     * Se usa en el inicio de sesión.
     * </p>
     */
    public Usuario findByCorreoYContrasena(String correo, String contrasena) throws SQLException {
        String sql = "SELECT id_usuario, nombre, correo, `contraseña` AS contrasena, id_rol "
                + "FROM usuario WHERE correo = ? AND `contraseña` = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, correo);
            stmt.setString(2, contrasena);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUsuario(rs);
                }
            }
        }

        return null;
    }

    private Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setNombre(rs.getString("nombre"));
        u.setCorreo(rs.getString("correo"));
        u.setContrasena(rs.getString("contrasena"));

        int idRol = rs.getInt("id_rol");
        u.setIdRol(rs.wasNull() ? null : idRol);

        return u;
    }
}
