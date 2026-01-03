package tugestionamiga.dao;

import tugestionamiga.config.DatabaseConnection;
import tugestionamiga.model.Libro;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para la entidad {@link tugestionamiga.model.Libro}.
 *
 * Contiene operaciones CRUD sobre la tabla {@code libro}.
 */
public class LibroDAO {

    /**
     * Inserta un libro en la base de datos.
     *
     * @return id generado o -1 si no se pudo obtener
     */
    public int create(Libro libro) throws SQLException {
        String sql = "INSERT INTO libro (titulo, autor, descripcion, disponibilidad, id_categoria) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
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
     * Busca un libro por su ID.
     */
    public Libro findById(int idLibro) throws SQLException {
        String sql = "SELECT id_libro, titulo, autor, descripcion, disponibilidad, id_categoria FROM libro WHERE id_libro = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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
     */
    public List<Libro> findAll() throws SQLException {
        String sql = "SELECT id_libro, titulo, autor, descripcion, disponibilidad, id_categoria FROM libro ORDER BY id_libro";
        List<Libro> libros = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
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
     */
    public boolean update(Libro libro) throws SQLException {
        String sql = "UPDATE libro SET titulo = ?, autor = ?, descripcion = ?, disponibilidad = ?, id_categoria = ? WHERE id_libro = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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
     * Actualiza únicamente la disponibilidad del libro.
     */
    public boolean updateDisponibilidad(int idLibro, boolean disponible) throws SQLException {
        String sql = "UPDATE libro SET disponibilidad = ? WHERE id_libro = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, disponible);
            stmt.setInt(2, idLibro);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un libro por ID.
     */
    public boolean delete(int idLibro) throws SQLException {
        String sql = "DELETE FROM libro WHERE id_libro = ?";

        try (Connection conn = DatabaseConnection.getConnection();
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
