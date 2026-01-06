package com.tugestionamiga.spring.repository;

import com.tugestionamiga.spring.model.Compra;
import com.tugestionamiga.spring.model.CompraPerfilRow;
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
 * Repositorio JDBC para la tabla {@code compra}.
 *
 * <p>
 * Permite listar compras y registrar una compra de forma transaccional.
 * El registro de compra también actualiza el inventario del libro:
 * </p>
 *
 * <ul>
 *   <li>Valida stock del libro.</li>
 *   <li>Inserta en {@code compra} (fecha, precio, usuario, libro).</li>
 *   <li>Descuenta N unidades de stock (según cantidad) y recalcula {@code disponibilidad}.</li>
 * </ul>
 */
@Repository
public class CompraRepository {

    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate tx;

    /**
     * Constructor con dependencias necesarias para consultas y transacciones.
     *
     * <p>
     * Se usa {@link TransactionTemplate} porque registrar compra debe ser atómico: insertar la compra
     * y descontar stock del libro.
     * </p>
     */
    public CompraRepository(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.tx = new TransactionTemplate(transactionManager);
    }

    /**
     * Lista todas las compras.
     *
     * <p>
     * Este listado se utiliza principalmente en modo ADMIN para ver el histórico completo.
     * </p>
     */
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

    /**
     * Lista compras filtradas por usuario.
     *
     * <p>
     * Existe para que un usuario normal solo vea sus propios registros.
     * </p>
     */
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

    /**
     * Variante de listado para la pantalla de perfil.
     *
     * <p>
     * Une la compra con el libro para mostrar título/autor sin consultas extra.
     * </p>
     */
    public List<CompraPerfilRow> findByUsuarioWithLibro(int idUsuario) {
        return jdbcTemplate.query(
                "SELECT c.id_compra, c.fecha_compra, c.precio, c.id_libro, l.titulo, l.autor "
                + "FROM compra c LEFT JOIN libro l ON l.id_libro = c.id_libro "
                + "WHERE c.id_usuario = ? ORDER BY c.id_compra",
                (rs, rowNum) -> {
                    CompraPerfilRow row = new CompraPerfilRow();
                    row.setIdCompra(rs.getInt("id_compra"));
                    row.setFechaCompra(rs.getDate("fecha_compra").toLocalDate());
                    row.setPrecio(rs.getBigDecimal("precio"));
                    int idLibro = rs.getInt("id_libro");
                    row.setIdLibro(rs.wasNull() ? null : idLibro);
                    row.setTitulo(rs.getString("titulo"));
                    row.setAutor(rs.getString("autor"));
                    return row;
                },
                idUsuario
        );
    }

    /**
     * Elimina una compra por id.
     *
     * <p>
     * Se usa como operación administrativa (por ejemplo, limpiar datos de prueba).
     * No revierte stock, porque el flujo normal de negocio no contempla “deshacer” compras.
     * </p>
     */
    public boolean delete(int idCompra) {
        return jdbcTemplate.update("DELETE FROM compra WHERE id_compra = ?", idCompra) > 0;
    }

    /**
     * Registra una compra e impacta el stock del libro en una única transacción.
     *
     * @return id de la compra creada o -1 si el libro no existe/no tiene stock
     */
    public int registrarCompra(int idUsuario, int idLibro, LocalDate fechaCompra, java.math.BigDecimal precio) {
        // Se conserva este método para no romper llamadas existentes.
        // Internamente delega a la versión con cantidad, usando 1 como valor por defecto.
        return registrarCompra(idUsuario, idLibro, fechaCompra, precio, 1);
    }

    /**
     * Registra una compra con una cantidad de ejemplares.
     *
     * <p>
     * La tabla actual registra una compra por fila, por eso se insertan N filas.
     * Luego se descuenta el stock en bloque (stock = stock - N).
     * </p>
     *
     * @return último id generado o -1 si no se pudo completar
     */
    public int registrarCompra(int idUsuario, int idLibro, LocalDate fechaCompra, java.math.BigDecimal precio, int cantidad) {
        return tx.execute(status -> {
            // Validación defensiva para evitar descuentos de stock inválidos.
            if (cantidad <= 0) {
                status.setRollbackOnly();
                return -1;
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

            // Se valida que el libro exista y que el stock alcance para la cantidad solicitada.
            if (stock == null || stock < cantidad) {
                status.setRollbackOnly();
                return -1;
            }

            int ultimoId = -1;
            for (int i = 0; i < cantidad; i++) {
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
                ultimoId = newId;
            }

            int newStock = stock - cantidad;
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

            return ultimoId;
        });
    }
}
