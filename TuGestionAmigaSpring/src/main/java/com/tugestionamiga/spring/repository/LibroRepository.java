package com.tugestionamiga.spring.repository;

import com.tugestionamiga.spring.model.Libro;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JDBC para la tabla {@code libro}.
 *
 * <p>
 * En esta versión se maneja inventario por medio de la columna {@code stock}.
 * La columna {@code disponibilidad} se recalcula automáticamente como:
 * </p>
 *
 * <pre>
 * disponibilidad = (stock &gt; 0)
 * </pre>
 */
@Repository
public class LibroRepository {

    private final JdbcTemplate jdbcTemplate;

    public LibroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Libro> findAll() {
        return jdbcTemplate.query(
                "SELECT l.id_libro, l.titulo, l.autor, l.descripcion, l.disponibilidad, l.stock, l.id_categoria, c.nombre_categoria "
                + "FROM libro l LEFT JOIN categoria c ON c.id_categoria = l.id_categoria ORDER BY l.id_libro",
                (rs, rowNum) -> {
                    Libro l = new Libro();
                    l.setIdLibro(rs.getInt("id_libro"));
                    l.setTitulo(rs.getString("titulo"));
                    l.setAutor(rs.getString("autor"));
                    l.setDescripcion(rs.getString("descripcion"));
                    l.setDisponible(rs.getBoolean("disponibilidad"));
                    l.setStock(rs.getInt("stock"));
                    int idCategoria = rs.getInt("id_categoria");
                    l.setIdCategoria(rs.wasNull() ? null : idCategoria);
                    l.setNombreCategoria(rs.getString("nombre_categoria"));
                    return l;
                }
        );
    }

    public Optional<Libro> findById(int idLibro) {
        List<Libro> list = jdbcTemplate.query(
                "SELECT l.id_libro, l.titulo, l.autor, l.descripcion, l.disponibilidad, l.stock, l.id_categoria, c.nombre_categoria "
                + "FROM libro l LEFT JOIN categoria c ON c.id_categoria = l.id_categoria WHERE l.id_libro = ?",
                (rs, rowNum) -> {
                    Libro l = new Libro();
                    l.setIdLibro(rs.getInt("id_libro"));
                    l.setTitulo(rs.getString("titulo"));
                    l.setAutor(rs.getString("autor"));
                    l.setDescripcion(rs.getString("descripcion"));
                    l.setDisponible(rs.getBoolean("disponibilidad"));
                    l.setStock(rs.getInt("stock"));
                    int idCategoria = rs.getInt("id_categoria");
                    l.setIdCategoria(rs.wasNull() ? null : idCategoria);
                    l.setNombreCategoria(rs.getString("nombre_categoria"));
                    return l;
                },
                idLibro
        );
        return list.stream().findFirst();
    }

    public int create(Libro libro) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO libro (titulo, autor, descripcion, stock, disponibilidad, id_categoria) VALUES (?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getDescripcion());
            ps.setInt(4, libro.getStock());
            ps.setBoolean(5, libro.getStock() > 0);
            if (libro.getIdCategoria() == null) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, libro.getIdCategoria());
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key == null ? -1 : key.intValue();
    }

    public boolean update(Libro libro) {
        int updated = jdbcTemplate.update(
                "UPDATE libro SET titulo = ?, autor = ?, descripcion = ?, stock = ?, disponibilidad = ?, id_categoria = ? WHERE id_libro = ?",
                ps -> {
                    ps.setString(1, libro.getTitulo());
                    ps.setString(2, libro.getAutor());
                    ps.setString(3, libro.getDescripcion());
                    ps.setInt(4, libro.getStock());
                    ps.setBoolean(5, libro.getStock() > 0);
                    if (libro.getIdCategoria() == null) {
                        ps.setNull(6, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(6, libro.getIdCategoria());
                    }
                    ps.setInt(7, libro.getIdLibro());
                }
        );
        return updated > 0;
    }

    public boolean delete(int idLibro) {
        return jdbcTemplate.update("DELETE FROM libro WHERE id_libro = ?", idLibro) > 0;
    }
}
