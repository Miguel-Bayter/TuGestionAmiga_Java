package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import model.Prestamo;
import util.ConexionBD;

/**
 * DAO para la entidad {@link model.Prestamo}.
 *
 * <p>
 * Además del CRUD, incluye dos operaciones importantes:
 * </p>
 *
 * <ul>
 *   <li>{@link #registrarPrestamo(int, int, LocalDate, LocalDate)}: inserta un préstamo y marca el libro como no disponible.</li>
 *   <li>{@link #registrarDevolucion(int)}: cambia el préstamo a DEVUELTO y vuelve a dejar el libro disponible.</li>
 * </ul>
 *
 * <p>
 * Ambas operaciones se realizan con transacciones para mantener consistencia.
 * </p>
 */
public class PrestamoDAO {

    private final ServletContext context;

    /**
     * Crea el DAO.
     *
     * @param context contexto de la aplicación (necesario para abrir conexiones)
     */
    public PrestamoDAO(ServletContext context) {
        this.context = context;
    }

    /**
     * Inserta un préstamo en la tabla {@code prestamo}.
     *
     * <p>
     * Este método es el CRUD “directo”. Para el flujo principal de negocio se recomienda
     * usar {@link #registrarPrestamo(int, int, LocalDate, LocalDate)}.
     * </p>
     *
     * @param prestamo datos del préstamo
     * @return id generado o -1 si no se pudo obtener
     * @throws SQLException si ocurre un error al insertar
     */
    public int create(Prestamo prestamo) throws SQLException {
        String sql = "INSERT INTO prestamo (fecha_prestamo, fecha_devolucion, estado, id_usuario, id_libro) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(prestamo.getFechaPrestamo()));
            stmt.setDate(2, Date.valueOf(prestamo.getFechaDevolucion()));
            stmt.setString(3, prestamo.getEstado());

            if (prestamo.getIdUsuario() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, prestamo.getIdUsuario());
            }

            if (prestamo.getIdLibro() == null) {
                stmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(5, prestamo.getIdLibro());
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
     * Busca un préstamo por id.
     *
     * @param idPrestamo id del préstamo
     * @return el préstamo o {@code null} si no existe
     * @throws SQLException si ocurre un error al consultar
     */
    public Prestamo findById(int idPrestamo) throws SQLException {
        String sql = "SELECT id_prestamo, fecha_prestamo, fecha_devolucion, estado, id_usuario, id_libro FROM prestamo WHERE id_prestamo = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPrestamo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPrestamo(rs);
                }
            }
        }

        return null;
    }

    /**
     * Lista todos los préstamos.
     *
     * @return lista de préstamos
     * @throws SQLException si ocurre un error al consultar
     */
    public List<Prestamo> findAll() throws SQLException {
        String sql = "SELECT id_prestamo, fecha_prestamo, fecha_devolucion, estado, id_usuario, id_libro FROM prestamo ORDER BY id_prestamo";
        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                prestamos.add(mapPrestamo(rs));
            }
        }

        return prestamos;
    }

    /**
     * Actualiza un préstamo.
     *
     * <p>
     * Este método actualiza todos los campos del préstamo. Para el flujo principal de devolución,
     * se recomienda usar {@link #registrarDevolucion(int)}.
     * </p>
     *
     * @param prestamo datos del préstamo
     * @return {@code true} si se actualizó al menos una fila
     * @throws SQLException si ocurre un error al actualizar
     */
    public boolean update(Prestamo prestamo) throws SQLException {
        String sql = "UPDATE prestamo SET fecha_prestamo = ?, fecha_devolucion = ?, estado = ?, id_usuario = ?, id_libro = ? WHERE id_prestamo = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(prestamo.getFechaPrestamo()));
            stmt.setDate(2, Date.valueOf(prestamo.getFechaDevolucion()));
            stmt.setString(3, prestamo.getEstado());

            if (prestamo.getIdUsuario() == null) {
                stmt.setNull(4, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(4, prestamo.getIdUsuario());
            }

            if (prestamo.getIdLibro() == null) {
                stmt.setNull(5, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(5, prestamo.getIdLibro());
            }

            stmt.setInt(6, prestamo.getIdPrestamo());
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un préstamo por id.
     *
     * @param idPrestamo id del préstamo
     * @return {@code true} si se eliminó
     * @throws SQLException si ocurre un error al eliminar
     */
    public boolean delete(int idPrestamo) throws SQLException {
        String sql = "DELETE FROM prestamo WHERE id_prestamo = ?";

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idPrestamo);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Registra un préstamo y actualiza la disponibilidad del libro.
     *
     * <p>
     * Flujo:
     * </p>
     *
     * <ol>
     *   <li>Bloquea la fila del libro (FOR UPDATE).</li>
     *   <li>Verifica que el libro esté disponible.</li>
     *   <li>Inserta el préstamo con estado ACTIVO.</li>
     *   <li>Actualiza el libro a no disponible.</li>
     * </ol>
     *
     * @return id del préstamo creado o -1 si no se pudo completar
     */
    public int registrarPrestamo(int idUsuario, int idLibro, LocalDate fechaPrestamo, LocalDate fechaDevolucion) throws SQLException {
        String checkSql = "SELECT disponibilidad FROM libro WHERE id_libro = ? FOR UPDATE";
        String insertSql = "INSERT INTO prestamo (fecha_prestamo, fecha_devolucion, estado, id_usuario, id_libro) VALUES (?, ?, ?, ?, ?)";
        String updateLibroSql = "UPDATE libro SET disponibilidad = 0 WHERE id_libro = ?";

        try (Connection conn = ConexionBD.getConnection(context)) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, idLibro);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return -1;
                    }

                    boolean disponible = rs.getBoolean("disponibilidad");
                    if (!disponible) {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            int newId;
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStmt.setDate(1, Date.valueOf(fechaPrestamo));
                insertStmt.setDate(2, Date.valueOf(fechaDevolucion));
                insertStmt.setString(3, "ACTIVO");
                insertStmt.setInt(4, idUsuario);
                insertStmt.setInt(5, idLibro);

                insertStmt.executeUpdate();

                try (ResultSet keys = insertStmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        newId = keys.getInt(1);
                    } else {
                        conn.rollback();
                        return -1;
                    }
                }
            }

            try (PreparedStatement updateLibroStmt = conn.prepareStatement(updateLibroSql)) {
                updateLibroStmt.setInt(1, idLibro);
                int updated = updateLibroStmt.executeUpdate();
                if (updated <= 0) {
                    conn.rollback();
                    return -1;
                }
            }

            conn.commit();
            return newId;
        }
    }

    /**
     * Registra la devolución de un préstamo.
     *
     * <p>
     * Cambia el estado del préstamo a DEVUELTO y vuelve a poner el libro como disponible.
     * </p>
     */
    public boolean registrarDevolucion(int idPrestamo) throws SQLException {
        String selectSql = "SELECT id_libro, estado FROM prestamo WHERE id_prestamo = ? FOR UPDATE";
        String updatePrestamoSql = "UPDATE prestamo SET estado = ? WHERE id_prestamo = ?";
        String updateLibroSql = "UPDATE libro SET disponibilidad = 1 WHERE id_libro = ?";

        try (Connection conn = ConexionBD.getConnection(context)) {
            conn.setAutoCommit(false);

            Integer idLibro;
            String estado;

            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, idPrestamo);

                try (ResultSet rs = selectStmt.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }

                    idLibro = rs.getInt("id_libro");
                    if (rs.wasNull()) {
                        conn.rollback();
                        return false;
                    }

                    estado = rs.getString("estado");
                }
            }

            if (estado != null && estado.equalsIgnoreCase("DEVUELTO")) {
                conn.rollback();
                return false;
            }

            try (PreparedStatement updatePrestamoStmt = conn.prepareStatement(updatePrestamoSql)) {
                updatePrestamoStmt.setString(1, "DEVUELTO");
                updatePrestamoStmt.setInt(2, idPrestamo);

                if (updatePrestamoStmt.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement updateLibroStmt = conn.prepareStatement(updateLibroSql)) {
                updateLibroStmt.setInt(1, idLibro);

                if (updateLibroStmt.executeUpdate() <= 0) {
                    conn.rollback();
                    return false;
                }
            }

            conn.commit();
            return true;
        }
    }

    private Prestamo mapPrestamo(ResultSet rs) throws SQLException {
        Prestamo p = new Prestamo();
        p.setIdPrestamo(rs.getInt("id_prestamo"));
        p.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());
        p.setFechaDevolucion(rs.getDate("fecha_devolucion").toLocalDate());
        p.setEstado(rs.getString("estado"));

        int idUsuario = rs.getInt("id_usuario");
        p.setIdUsuario(rs.wasNull() ? null : idUsuario);

        int idLibro = rs.getInt("id_libro");
        p.setIdLibro(rs.wasNull() ? null : idLibro);

        return p;
    }
}
