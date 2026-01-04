package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import model.Libro;
import util.ConexionBD;

/**
 * DAO para la entidad {@link model.Libro}.
 *
 * <p>
 * Maneja el CRUD sobre la tabla {@code libro}. También permite actualizar solo el campo
 * {@code disponibilidad} cuando se presta o se devuelve un libro.
 * </p>
 */
public class LibroDAO {

    private final ServletContext context;

    /**
     * Crea el DAO.
     *
     * @param context contexto de la aplicación (necesario para abrir conexiones)
     */
    public LibroDAO(ServletContext context) {
        this.context = context;
    }

    /**
     * Inserta un libro en la tabla {@code libro}.
     *
     * @param libro datos del libro
     * @return id generado o -1 si no se pudo obtener
     * @throws SQLException si ocurre un error al insertar
     */
    public int create(Libro libro) throws SQLException {
        String sql = "INSERT INTO libro (titulo, autor, descripcion, disponibilidad, id_categoria) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getAutor());
            stmt.setString(3, libro.getDescripcion());
            stmt.setBoolean(4, libro.isDisponible());

            if (libro.getIdCategoria() == null) {
                stmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(5, libro.getIdCategoria());
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
     * Busca un libro por su id.
     *
     * @param idLibro id del libro
     * @return libro encontrado o {@code null} si no existe
     * @throws SQLException si ocurre un error al consultar
     */
    public Libro findById(int idLibro) throws SQLException {
        String sql = "SELECT id_libro, titulo, autor, descripcion, disponibilidad, id_categoria FROM libro WHERE id_libro = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idLibro);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapLibro(rs);
                }
            }
        }

        return null;
    }

    /**
     * Lista todos los libros.
     *
     * @return lista de libros
     * @throws SQLException si ocurre un error al consultar
     */
    public List<Libro> findAll() throws SQLException {
        String sql = "SELECT id_libro, titulo, autor, descripcion, disponibilidad, id_categoria FROM libro ORDER BY id_libro";
        List<Libro> libros = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                libros.add(mapLibro(rs));
            }
        }

        return libros;
    }

    /**
     * Actualiza los datos de un libro.
     *
     * @param libro datos nuevos del libro
     * @return {@code true} si se actualizó al menos una fila
     * @throws SQLException si ocurre un error al actualizar
     */
    public boolean update(Libro libro) throws SQLException {
        String sql = "UPDATE libro SET titulo = ?, autor = ?, descripcion = ?, disponibilidad = ?, id_categoria = ? WHERE id_libro = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libro.getTitulo());
            stmt.setString(2, libro.getAutor());
            stmt.setString(3, libro.getDescripcion());
            stmt.setBoolean(4, libro.isDisponible());

            if (libro.getIdCategoria() == null) {
                stmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(5, libro.getIdCategoria());
            }

            stmt.setInt(6, libro.getIdLibro());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Actualiza únicamente la disponibilidad de un libro.
     *
     * <p>
     * Esto se usa cuando se registra un préstamo (se pone no disponible) o una devolución
     * (se vuelve a poner disponible).
     * </p>
     */
    public boolean updateDisponibilidad(int idLibro, boolean disponible) throws SQLException {
        String sql = "UPDATE libro SET disponibilidad = ? WHERE id_libro = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, disponible);
            stmt.setInt(2, idLibro);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un libro por id.
     *
     * @param idLibro id del libro
     * @return {@code true} si se eliminó
     * @throws SQLException si ocurre un error al eliminar
     */
    public boolean delete(int idLibro) throws SQLException {
        String sql = "DELETE FROM libro WHERE id_libro = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idLibro);
            return stmt.executeUpdate() > 0;
        }
    }

    private Libro mapLibro(ResultSet rs) throws SQLException {
        Libro libro = new Libro();
        libro.setIdLibro(rs.getInt("id_libro"));
        libro.setTitulo(rs.getString("titulo"));
        libro.setAutor(rs.getString("autor"));
        libro.setDescripcion(rs.getString("descripcion"));
        libro.setDisponible(rs.getBoolean("disponibilidad"));

        int idCategoria = rs.getInt("id_categoria");
        libro.setIdCategoria(rs.wasNull() ? null : idCategoria);

        return libro;
    }
}
