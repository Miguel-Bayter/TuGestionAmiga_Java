package com.tugestionamiga.spring.repository;

import com.tugestionamiga.spring.model.Prestamo;
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

    public boolean delete(int idPrestamo) {
        return jdbcTemplate.update("DELETE FROM prestamo WHERE id_prestamo = ?", idPrestamo) > 0;
    }

    public int registrarPrestamo(int idUsuario, int idLibro, LocalDate fechaPrestamo, LocalDate fechaDevolucion) {
        return tx.execute(status -> {
            Boolean disponible = jdbcTemplate.query(
                    "SELECT disponibilidad FROM libro WHERE id_libro = ? FOR UPDATE",
                    rs -> {
                        if (!rs.next()) {
                            return null;
                        }
                        return rs.getBoolean("disponibilidad");
                    },
                    idLibro
            );

            if (disponible == null || !disponible) {
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

            int updated = jdbcTemplate.update("UPDATE libro SET disponibilidad = 0 WHERE id_libro = ?", idLibro);
            if (updated <= 0) {
                status.setRollbackOnly();
                return -1;
            }

            return newId;
        });
    }

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

            int upLibro = jdbcTemplate.update("UPDATE libro SET disponibilidad = 1 WHERE id_libro = ?", idLibro);
            if (upLibro <= 0) {
                status.setRollbackOnly();
                return false;
            }

            return true;
        }));
    }
}
