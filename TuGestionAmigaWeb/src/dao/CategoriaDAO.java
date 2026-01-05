package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import model.Categoria;
import util.ConexionBD;

/**
 * DAO para consultar categorías (géneros) desde la tabla {@code categoria}.
 *
 * <p>
 * En el módulo web se usa principalmente para cargar el catálogo de géneros y mostrarlo
 * en el formulario de creación/edición de libros.
 * </p>
 */
public class CategoriaDAO {

    private final ServletContext context;

    public CategoriaDAO(ServletContext context) {
        this.context = context;
    }

    public List<Categoria> findAll() throws SQLException {
        String sql = "SELECT id_categoria, nombre_categoria FROM categoria ORDER BY id_categoria";
        List<Categoria> categorias = new ArrayList<>();

        try (Connection conn = ConexionBD.getConnection(context);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Categoria c = new Categoria();
                c.setIdCategoria(rs.getInt("id_categoria"));
                c.setNombreCategoria(rs.getString("nombre_categoria"));
                categorias.add(c);
            }
        }

        return categorias;
    }
}
