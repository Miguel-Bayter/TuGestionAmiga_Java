package tugestionamiga.dao;

import tugestionamiga.config.DatabaseConnection;
import tugestionamiga.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad {@link tugestionamiga.model.Usuario}.
 *
 * Contiene operaciones CRUD sobre la tabla {@code usuario}.
 *
 * Nota: el esquema usa la columna {@code contraseña} (con ñ). Para evitar
 * problemas al leerla desde Java, en los SELECT se usa alias:
 * {@code `contraseña` AS contrasena}.
 */
public class UsuarioDAO {

    /**
     * Inserta un usuario en la base de datos.
     *
     * @param usuario usuario a insertar
     * @return id generado o -1 si no se pudo obtener
     */
    public int create(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuario (nombre, correo, `contraseña`, id_rol) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
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
     *
     * @param idUsuario id del usuario
     * @return usuario encontrado o null si no existe
     */
    public Usuario findById(int idUsuario) throws SQLException {
        String sql = "SELECT id_usuario, nombre, correo, `contraseña` AS contrasena, id_rol FROM usuario WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
        }

        return null;
    }

    /**
     * Lista todos los usuarios.
     *
     * @return lista de usuarios
     */
    public List<Usuario> findAll() throws SQLException {
        String sql = "SELECT id_usuario, nombre, correo, `contraseña` AS contrasena, id_rol FROM usuario ORDER BY id_usuario";
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre"));
                u.setCorreo(rs.getString("correo"));
                u.setContrasena(rs.getString("contrasena"));

                int idRol = rs.getInt("id_rol");
                u.setIdRol(rs.wasNull() ? null : idRol);

                usuarios.add(u);
            }
        }

        return usuarios;
    }

    /**
     * Actualiza los datos de un usuario.
     *
     * @param usuario usuario con los datos nuevos (incluyendo id)
     * @return true si se actualizó al menos un registro
     */
    public boolean update(Usuario usuario) throws SQLException {
        String sql = "UPDATE usuario SET nombre = ?, correo = ?, `contraseña` = ?, id_rol = ? WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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
     *
     * @param idUsuario id del usuario
     * @return true si se eliminó al menos un registro
     */
    public boolean delete(int idUsuario) throws SQLException {
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idUsuario);
            return stmt.executeUpdate() > 0;
        }
    }
}
