package com.tugestionamiga.spring.repository;

import com.tugestionamiga.spring.model.Usuario;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JDBC para la tabla {@code usuario}.
 *
 * <p>
 * Existe para concentrar las consultas SQL de usuarios en un solo lugar y evitar que los controllers
 * tengan SQL incrustado.
 * </p>
 *
 * <p>
 * Se usa {@link JdbcTemplate} en vez de JPA/Hibernate porque el proyecto ya tenía un esquema y una
 * lógica basada en SQL directo. Además, esto permite controlar explícitamente consultas como la
 * lectura de la columna {@code `contraseña`} (que incluye la ñ en la base).
 * </p>
 */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Usuario> findAll() {
        return jdbcTemplate.query(
                "SELECT id_usuario, nombre, correo, `contraseña` AS contrasena, id_rol FROM usuario ORDER BY id_usuario",
                (rs, rowNum) -> {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setCorreo(rs.getString("correo"));
                    u.setContrasena(rs.getString("contrasena"));
                    int idRol = rs.getInt("id_rol");
                    u.setIdRol(rs.wasNull() ? null : idRol);
                    return u;
                }
        );
    }

    /**
     * Busca un usuario por id.
     *
     * <p>
     * Se usa para la pantalla de edición y para validaciones de negocio.
     * </p>
     */
    public Optional<Usuario> findById(int idUsuario) {
        List<Usuario> list = jdbcTemplate.query(
                "SELECT id_usuario, nombre, correo, `contraseña` AS contrasena, id_rol FROM usuario WHERE id_usuario = ?",
                (rs, rowNum) -> {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNombre(rs.getString("nombre"));
                    u.setCorreo(rs.getString("correo"));
                    u.setContrasena(rs.getString("contrasena"));
                    int idRol = rs.getInt("id_rol");
                    u.setIdRol(rs.wasNull() ? null : idRol);
                    return u;
                },
                idUsuario
        );
        return list.stream().findFirst();
    }

    /**
     * Obtiene el id del usuario a partir del correo.
     *
     * <p>
     * Esto se utiliza para relacionar la sesión de Spring Security (que identifica por correo)
     * con los registros reales de la tabla {@code usuario}.
     * </p>
     */
    public Optional<Integer> findIdByCorreo(String correo) {
        List<Integer> ids = jdbcTemplate.query(
                "SELECT id_usuario FROM usuario WHERE correo = ?",
                (rs, rowNum) -> rs.getInt("id_usuario"),
                correo
        );
        return ids.stream().findFirst();
    }

    /**
     * Inserta un usuario y devuelve el id generado.
     */
    public int create(Usuario usuario) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO usuario (nombre, correo, `contraseña`, id_rol) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, usuario.getNombre());
            ps.setString(2, usuario.getCorreo());
            ps.setString(3, usuario.getContrasena());
            if (usuario.getIdRol() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, usuario.getIdRol());
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key == null ? -1 : key.intValue();
    }

    /**
     * Actualiza un usuario existente.
     *
     * <p>
     * Se actualizan nombre, correo, contraseña y rol. Esto permite que el módulo de administración
     * cambie credenciales y que el rol se refleje en la seguridad.
     * </p>
     */
    public boolean update(Usuario usuario) {
        int updated = jdbcTemplate.update(
                "UPDATE usuario SET nombre = ?, correo = ?, `contraseña` = ?, id_rol = ? WHERE id_usuario = ?",
                ps -> {
                    ps.setString(1, usuario.getNombre());
                    ps.setString(2, usuario.getCorreo());
                    ps.setString(3, usuario.getContrasena());
                    if (usuario.getIdRol() == null) {
                        ps.setNull(4, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(4, usuario.getIdRol());
                    }
                    ps.setInt(5, usuario.getIdUsuario());
                }
        );
        return updated > 0;
    }

    /**
     * Elimina un usuario por id.
     */
    public boolean delete(int idUsuario) {
        return jdbcTemplate.update("DELETE FROM usuario WHERE id_usuario = ?", idUsuario) > 0;
    }
}
