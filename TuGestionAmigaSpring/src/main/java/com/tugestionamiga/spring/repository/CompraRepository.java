package com.tugestionamiga.spring.repository;

import com.tugestionamiga.spring.model.Compra;
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
public class CompraRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate tx;

    public CompraRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.tx = new TransactionTemplate(transactionManager);
    }

    public List<Compra> findAll() {
        return jdbcTemplate.query(
                "SELECT id_compra, fecha_compra, precio, id_usuario, id_libro FROM compra ORDER BY id_compra",
                (rs, rowNum) -> {
                    Compra c = new Compra();
                    c.setIdCompra(rs.getInt("id_compra"));
                    c.setFechaCompra(rs.getDate("fecha_compra").toLocalDate());
                    c.setPrecio(rs.getBigDecimal("precio"));
                    int idUsuario = rs.getInt("id_usuario");
                    c.setIdUsuario(rs.wasNull() ? null : idUsuario);
                    int idLibro = rs.getInt("id_libro");
                    c.setIdLibro(rs.wasNull() ? null : idLibro);
                    return c;
                }
        );
    }

    public List<Compra> findByUsuario(int idUsuario) {
        return jdbcTemplate.query(
                "SELECT id_compra, fecha_compra, precio, id_usuario, id_libro FROM compra WHERE id_usuario = ? ORDER BY id_compra",
                (rs, rowNum) -> {
                    Compra c = new Compra();
                    c.setIdCompra(rs.getInt("id_compra"));
                    c.setFechaCompra(rs.getDate("fecha_compra").toLocalDate());
                    c.setPrecio(rs.getBigDecimal("precio"));
                    int idU = rs.getInt("id_usuario");
                    c.setIdUsuario(rs.wasNull() ? null : idU);
                    int idL = rs.getInt("id_libro");
                    c.setIdLibro(rs.wasNull() ? null : idL);
                    return c;
                },
                idUsuario
        );
    }

    public boolean delete(int idCompra) {
        return jdbcTemplate.update("DELETE FROM compra WHERE id_compra = ?", idCompra) > 0;
    }

    public int registrarCompra(int idUsuario, int idLibro, LocalDate fechaCompra, java.math.BigDecimal precio) {
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
                        "INSERT INTO compra (fecha_compra, precio, id_usuario, id_libro) VALUES (?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS
                );
                ps.setDate(1, Date.valueOf(fechaCompra));
                ps.setBigDecimal(2, precio);
                ps.setInt(3, idUsuario);
                ps.setInt(4, idLibro);
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
}
