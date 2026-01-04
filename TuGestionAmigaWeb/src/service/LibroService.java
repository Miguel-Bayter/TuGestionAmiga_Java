package service;

import dao.LibroDAO;
import javax.servlet.ServletContext;
import model.Libro;

import java.sql.SQLException;
import java.util.List;

/**
 * Servicio para libros.
 *
 * <p>
 * Se usa para dejar el servlet más limpio y que el DAO quede solo para SQL.
 * </p>
 */
public class LibroService {

    private final LibroDAO libroDAO;

    public LibroService(ServletContext context) {
        this.libroDAO = new LibroDAO(context);
    }

    public List<Libro> listarLibros() throws SQLException {
        return libroDAO.findAll();
    }

    public Libro obtenerPorId(int idLibro) throws SQLException {
        return libroDAO.findById(idLibro);
    }

    public int crearLibro(Libro libro) throws SQLException {
        return libroDAO.create(libro);
    }

    public boolean actualizarLibro(Libro libro) throws SQLException {
        return libroDAO.update(libro);
    }

    public boolean eliminarLibro(int idLibro) throws SQLException {
        return libroDAO.delete(idLibro);
    }
}
