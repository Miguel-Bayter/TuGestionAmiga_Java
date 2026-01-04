package service;

import dao.UsuarioDAO;
import javax.servlet.ServletContext;
import model.Usuario;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio para centralizar la lógica de usuarios.
 *
 * <p>
 * La idea es que los servlets no tengan que mezclar validaciones y reglas con acceso a datos.
 * Aquí se deja esa lógica en un solo lugar y el servlet solo coordina la petición/respuesta.
 * </p>
 */
public class UsuarioService {

    private final UsuarioDAO usuarioDAO;

    public UsuarioService(ServletContext context) {
        this.usuarioDAO = new UsuarioDAO(context);
    }

    /**
     * Obtiene todos los usuarios.
     *
     * @return lista de usuarios (puede venir vacía)
     */
    public List<Usuario> listarUsuarios() throws SQLException {
        return usuarioDAO.findAll();
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param idUsuario id de usuario
     * @return el usuario encontrado o {@code null} si no existe
     */
    public Usuario obtenerPorId(int idUsuario) throws SQLException {
        return usuarioDAO.findById(idUsuario);
    }

    /**
     * Busca un usuario por correo.
     *
     * <p>
     * Se usa en varias validaciones (por ejemplo, evitar correos duplicados en registro).
     * Si el correo viene vacío, retorna {@code null}.
     * </p>
     */
    public Usuario buscarPorCorreo(String correo) throws SQLException {
        if (correo == null || correo.trim().isEmpty()) {
            return null;
        }
        return usuarioDAO.findByCorreo(correo.trim());
    }

    /**
     * Autentica un usuario por correo y contraseña.
     *
     * @return el usuario si las credenciales coinciden, o {@code null} si no coincide
     */
    public Usuario autenticar(String correo, String contrasena) throws SQLException {
        if (correo == null || correo.trim().isEmpty() || contrasena == null || contrasena.trim().isEmpty()) {
            return null;
        }
        return usuarioDAO.findByCorreoYContrasena(correo.trim(), contrasena.trim());
    }

    public int registrarUsuarioPublico(String nombre, String correo, String contrasena, String contrasena2) throws SQLException {
        // Validación básica: no permitir campos vacíos.
        if (nombre == null || nombre.trim().isEmpty()
                || correo == null || correo.trim().isEmpty()
                || contrasena == null || contrasena.trim().isEmpty()
                || contrasena2 == null || contrasena2.trim().isEmpty()) {
            throw new IllegalArgumentException("Debes completar todos los campos.");
        }

        // Validación básica: que ambas contraseñas coincidan.
        if (!contrasena.trim().equals(contrasena2.trim())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }

        // Se valida que el correo sea único.
        Usuario existente = usuarioDAO.findByCorreo(correo.trim());
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese correo.");
        }

        Usuario u = new Usuario();
        u.setNombre(nombre.trim());
        u.setCorreo(correo.trim());
        u.setContrasena(contrasena.trim());
        u.setIdRol(null);

        return usuarioDAO.create(u);
    }

    /**
     * Crea un usuario desde el CRUD interno.
     *
     * <p>
     * Este método asume que el servlet ya armó el objeto con los campos necesarios.
     * Si se quisieran más reglas (por ejemplo, correo único en el CRUD), se podrían centralizar aquí.
     * </p>
     */
    public int crearUsuario(Usuario u) throws SQLException {
        return usuarioDAO.create(u);
    }

    /**
     * Actualiza un usuario.
     */
    public boolean actualizarUsuario(Usuario u) throws SQLException {
        return usuarioDAO.update(u);
    }

    /**
     * Elimina un usuario por ID.
     */
    public boolean eliminarUsuario(int idUsuario) throws SQLException {
        return usuarioDAO.delete(idUsuario);
    }
}
