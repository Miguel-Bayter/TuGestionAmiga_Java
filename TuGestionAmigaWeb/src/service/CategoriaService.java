package service;

import dao.CategoriaDAO;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletContext;
import model.Categoria;

/**
 * Servicio de categorías.
 *
 * <p>
 * Se agregó para mantener la misma separación por capas del proyecto web:
 * el Servlet no consulta SQL directamente, sino que delega en el Service, y el Service
 * delega en el DAO.
 * </p>
 */
public class CategoriaService {

    private final CategoriaDAO categoriaDAO;

    public CategoriaService(ServletContext context) {
        this.categoriaDAO = new CategoriaDAO(context);
    }

    public List<Categoria> listarCategorias() throws SQLException {
        return categoriaDAO.findAll();
    }
}
