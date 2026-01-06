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

    /**
     * Constructor con inyección de {@link JdbcTemplate}.
     *
     * <p>
     * Se inyecta para centralizar el acceso a BD y reutilizar el {@code DataSource} configurado por
     * Spring Boot.
     * </p>
     */
    public LibroRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Lista todos los libros con su categoría (si existe).
     *
     * <p>
     * Se usa en pantallas como Libros, Compras y Préstamos para poblar el selector y el listado.
     * Se hace {@code LEFT JOIN} con {@code categoria} para mostrar el nombre sin otra consulta.
     * </p>
     */
    public List<Libro> findAll() {
        return jdbcTemplate.query(
                "SELECT l.id_libro, l.titulo, l.autor, l.descripcion, l.disponibilidad, l.stock, l.valor, l.id_categoria, c.nombre_categoria "
                + "FROM libro l LEFT JOIN categoria c ON c.id_categoria = l.id_categoria ORDER BY l.id_libro",
                (rs, rowNum) -> {
                    Libro l = new Libro();
                    l.setIdLibro(rs.getInt("id_libro"));
                    l.setTitulo(rs.getString("titulo"));
                    l.setAutor(rs.getString("autor"));
                    l.setDescripcion(rs.getString("descripcion"));
                    l.setDisponible(rs.getBoolean("disponibilidad"));
                    l.setStock(rs.getInt("stock"));
                    l.setValor(rs.getBigDecimal("valor"));
                    int idCategoria = rs.getInt("id_categoria");
                    l.setIdCategoria(rs.wasNull() ? null : idCategoria);
                    l.setNombreCategoria(rs.getString("nombre_categoria"));
                    return l;
                }
        );
    }

    /**
     * Busca un libro por id.
     *
     * <p>
     * Existe para reutilizarse en validaciones (por ejemplo: ver stock antes de prestar/comprar)
     * y para la pantalla de edición.
     * </p>
     */
    public Optional<Libro> findById(int idLibro) {
        List<Libro> list = jdbcTemplate.query(
                "SELECT l.id_libro, l.titulo, l.autor, l.descripcion, l.disponibilidad, l.stock, l.valor, l.id_categoria, c.nombre_categoria "
                + "FROM libro l LEFT JOIN categoria c ON c.id_categoria = l.id_categoria WHERE l.id_libro = ?",
                (rs, rowNum) -> {
                    Libro l = new Libro();
                    l.setIdLibro(rs.getInt("id_libro"));
                    l.setTitulo(rs.getString("titulo"));
                    l.setAutor(rs.getString("autor"));
                    l.setDescripcion(rs.getString("descripcion"));
                    l.setDisponible(rs.getBoolean("disponibilidad"));
                    l.setStock(rs.getInt("stock"));
                    l.setValor(rs.getBigDecimal("valor"));
                    int idCategoria = rs.getInt("id_categoria");
                    l.setIdCategoria(rs.wasNull() ? null : idCategoria);
                    l.setNombreCategoria(rs.getString("nombre_categoria"));
                    return l;
                },
                idLibro
        );
        return list.stream().findFirst();
    }

    /**
     * Crea un libro y devuelve el id generado.
     *
     * <p>
     * La disponibilidad se calcula a partir del stock para mantener una regla única:
     * {@code disponible = stock > 0}.
     * </p>
     */
    public int create(Libro libro) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO libro (titulo, autor, descripcion, stock, valor, disponibilidad, id_categoria) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, libro.getTitulo());
            ps.setString(2, libro.getAutor());
            ps.setString(3, libro.getDescripcion());
            ps.setInt(4, libro.getStock());
            // Si el valor no está definido, se guarda como 0.00 para evitar null en la BD.
            ps.setBigDecimal(5, (libro.getValor() == null) ? java.math.BigDecimal.ZERO : libro.getValor());
            ps.setBoolean(6, libro.getStock() > 0);
            if (libro.getIdCategoria() == null) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, libro.getIdCategoria());
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        return key == null ? -1 : key.intValue();
    }

    /**
     * Actualiza los datos de un libro existente.
     *
     * <p>
     * Se actualiza también {@code disponibilidad} en función del {@code stock} para evitar estados
     * inconsistentes (por ejemplo, stock 0 pero disponible en 1).
     * </p>
     */
    public boolean update(Libro libro) {
        int updated = jdbcTemplate.update(
                "UPDATE libro SET titulo = ?, autor = ?, descripcion = ?, stock = ?, valor = ?, disponibilidad = ?, id_categoria = ? WHERE id_libro = ?",
                ps -> {
                    ps.setString(1, libro.getTitulo());
                    ps.setString(2, libro.getAutor());
                    ps.setString(3, libro.getDescripcion());
                    ps.setInt(4, libro.getStock());
                    // El valor se mantiene como dato del catálogo (lo define administración).
                    ps.setBigDecimal(5, (libro.getValor() == null) ? java.math.BigDecimal.ZERO : libro.getValor());
                    ps.setBoolean(6, libro.getStock() > 0);
                    if (libro.getIdCategoria() == null) {
                        ps.setNull(7, java.sql.Types.INTEGER);
                    } else {
                        ps.setInt(7, libro.getIdCategoria());
                    }
                    ps.setInt(8, libro.getIdLibro());
                }
        );
        return updated > 0;
    }

    /**
     * Elimina un libro por id.
     *
     * <p>
     * Se usa solo desde administración. Si existen compras/préstamos asociados, la BD puede
     * rechazar la operación por llaves foráneas.
     * </p>
     */
    public boolean delete(int idLibro) {
        return jdbcTemplate.update("DELETE FROM libro WHERE id_libro = ?", idLibro) > 0;
    }
}
