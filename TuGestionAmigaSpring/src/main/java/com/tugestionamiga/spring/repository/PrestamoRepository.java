package com.tugestionamiga.spring.repository;

import com.tugestionamiga.spring.model.Prestamo;
import com.tugestionamiga.spring.model.PrestamoPerfilRow;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Repositorio JDBC para la entidad {@code prestamo}.
 *
 * <p>
 * Además de los listados básicos, contiene la lógica principal del negocio:
 * </p>
 *
 * <ul>
 *   <li><strong>Registrar préstamo</strong>: valida stock del libro, inserta el préstamo y descuenta stock.</li>
 *   <li><strong>Registrar devolución</strong>: marca el préstamo como DEVUELTO y repone stock del libro.</li>
 * </ul>
 *
 * <p>
 * Estas operaciones se ejecutan dentro de una transacción para evitar inconsistencias.
 * Se usa {@code FOR UPDATE} para bloquear la fila del libro mientras se actualiza el stock.
 * </p>
 */
@Repository
public class PrestamoRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate tx;

    public PrestamoRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public List<Prestamo> findAll() {
        return jdbcTemplate.query(
                "SELECT id_prestamo, fecha_prestamo, fecha_devolucion, estado, id_usuario, id_libro FROM prestamo ORDER BY id_prestamo",
                (rs, rowNum) -> {
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
        );
    }

    public List<Prestamo> findByUsuario(int idUsuario) {
        return jdbcTemplate.query(
                "SELECT id_prestamo, fecha_prestamo, fecha_devolucion, estado, id_usuario, id_libro "
                + "FROM prestamo WHERE id_usuario = ? ORDER BY id_prestamo",
                (rs, rowNum) -> {
                    Prestamo p = new Prestamo();
                    p.setIdPrestamo(rs.getInt("id_prestamo"));
                    p.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());
                    p.setFechaDevolucion(rs.getDate("fecha_devolucion").toLocalDate());
                    p.setEstado(rs.getString("estado"));
                    int idU = rs.getInt("id_usuario");
                    p.setIdUsuario(rs.wasNull() ? null : idU);
                    int idL = rs.getInt("id_libro");
                    p.setIdLibro(rs.wasNull() ? null : idL);
                    return p;
                },
                idUsuario
        );
    }

    /**
     * Variante de listado para la pantalla de perfil.
     *
     * <p>
     * Trae los préstamos del usuario junto a datos del libro (título/autor) para evitar
     * que la vista tenga que hacer consultas adicionales.
     * </p>
     */
    public List<PrestamoPerfilRow> findByUsuarioWithLibro(int idUsuario) {
        return jdbcTemplate.query(
                "SELECT p.id_prestamo, p.fecha_prestamo, p.fecha_devolucion, p.estado, p.id_libro, l.titulo, l.autor "
                + "FROM prestamo p LEFT JOIN libro l ON l.id_libro = p.id_libro "
                + "WHERE p.id_usuario = ? ORDER BY p.id_prestamo",
                (rs, rowNum) -> {
                    PrestamoPerfilRow row = new PrestamoPerfilRow();
                    row.setIdPrestamo(rs.getInt("id_prestamo"));
                    row.setFechaPrestamo(rs.getDate("fecha_prestamo").toLocalDate());
                    row.setFechaDevolucion(rs.getDate("fecha_devolucion").toLocalDate());
                    row.setEstado(rs.getString("estado"));
                    int idLibro = rs.getInt("id_libro");
                    row.setIdLibro(rs.wasNull() ? null : idLibro);
                    row.setTitulo(rs.getString("titulo"));
                    row.setAutor(rs.getString("autor"));
                    return row;
                },
                idUsuario
        );
    }

    public boolean delete(int idPrestamo) {
        return jdbcTemplate.update("DELETE FROM prestamo WHERE id_prestamo = ?", idPrestamo) > 0;
    }

    /**
     * Registra un préstamo en estado {@code ACTIVO} y descuenta 1 unidad del stock del libro.
     *
     * <p>
     * Flujo:
     * </p>
     *
     * <ol>
     *   <li>Bloquea el libro (FOR UPDATE) y obtiene stock.</li>
     *   <li>Valida stock &gt; 0.</li>
     *   <li>Inserta el préstamo.</li>
     *   <li>Actualiza libro: stock = stock - 1 y disponibilidad = (stock &gt; 0).</li>
     * </ol>
     *
     * @return id del préstamo creado o -1 si no se pudo completar
     */
    public int registrarPrestamo(int idUsuario, int idLibro, LocalDate fechaPrestamo, LocalDate fechaDevolucion) {
        return tx.execute(status -> {
            Integer stock = jdbcTemplate.query(
                    "SELECT stock FROM libro WHERE id_libro = ? FOR UPDATE",
                    rs -> {
                        if (!rs.next()) {
                            return null;
                        }
                        return rs.getInt("stock");
                    },
                    idLibro
            );

            if (stock == null || stock <= 0) {
                status.setRollbackOnly();
                return -1;
            }

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(
                        "INSERT INTO prestamo (fecha_prestamo, fecha_devolucion, estado, id_usuario, id_libro) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setDate(1, Date.valueOf(fechaPrestamo));
                ps.setDate(2, Date.valueOf(fechaDevolucion));
                ps.setString(3, "ACTIVO");
                ps.setInt(4, idUsuario);
                ps.setInt(5, idLibro);
                return ps;
            }, keyHolder);

            Number key = keyHolder.getKey();
            int newId = key == null ? -1 : key.intValue();
            if (newId <= 0) {
                status.setRollbackOnly();
                return -1;
            }

            int newStock = stock - 1;
            int updated = jdbcTemplate.update(
                    "UPDATE libro SET stock = ?, disponibilidad = ? WHERE id_libro = ?",
                    newStock,
                    newStock > 0,
                    idLibro
            );
            if (updated <= 0) {
                status.setRollbackOnly();
                return -1;
            }

            return newId;
        });
    }

    /**
     * Registra la devolución de un préstamo.
     *
     * <p>
     * Cambia el préstamo a {@code DEVUELTO} y repone 1 unidad de stock del libro.
     * Si el préstamo ya estaba DEVUELTO se rechaza la operación.
     * </p>
     */
    public boolean registrarDevolucion(int idPrestamo) {
        return Boolean.TRUE.equals(tx.execute(status -> {
            Integer idLibro = jdbcTemplate.query(
                    "SELECT id_libro, estado FROM prestamo WHERE id_prestamo = ? FOR UPDATE",
                    rs -> {
                        if (!rs.next()) {
                            return null;
                        }
                        String estado = rs.getString("estado");
                        if (estado != null && estado.equalsIgnoreCase("DEVUELTO")) {
                            return -1;
                        }
                        int idL = rs.getInt("id_libro");
                        return rs.wasNull() ? null : idL;
                    },
                    idPrestamo
            );

            if (idLibro == null || idLibro <= 0) {
                status.setRollbackOnly();
                return false;
            }

            int upPrestamo = jdbcTemplate.update("UPDATE prestamo SET estado = ? WHERE id_prestamo = ?", "DEVUELTO", idPrestamo);
            if (upPrestamo <= 0) {
                status.setRollbackOnly();
                return false;
            }

            Integer stock = jdbcTemplate.query(
                    "SELECT stock FROM libro WHERE id_libro = ? FOR UPDATE",
                    rs -> {
                        if (!rs.next()) {
                            return null;
                        }
                        return rs.getInt("stock");
                    },
                    idLibro
            );

            if (stock == null) {
                status.setRollbackOnly();
                return false;
            }

            int newStock = stock + 1;
            int upLibro = jdbcTemplate.update(
                    "UPDATE libro SET stock = ?, disponibilidad = ? WHERE id_libro = ?",
                    newStock,
                    newStock > 0,
                    idLibro
            );
            if (upLibro <= 0) {
                status.setRollbackOnly();
                return false;
            }

            return true;
        }));
    }
}
