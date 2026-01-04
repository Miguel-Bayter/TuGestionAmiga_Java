package service;

import dao.PrestamoDAO;
import javax.servlet.ServletContext;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import model.Prestamo;

/**
 * Servicio de préstamos.
 *
 * <p>
 * Aquí se centralizan las operaciones principales del negocio (prestar/devolver) para que
 * el servlet solo reciba parámetros y muestre mensajes.
 * </p>
 */
public class PrestamoService {

    private final PrestamoDAO prestamoDAO;

    public PrestamoService(ServletContext context) {
        this.prestamoDAO = new PrestamoDAO(context);
    }

    public List<Prestamo> listarPrestamos() throws SQLException {
        return prestamoDAO.findAll();
    }

    /**
     * Lista préstamos por usuario.
     *
     * <p>
     * Se usa para el modo "usuario" del módulo web, donde el usuario solo debe ver sus propios
     * préstamos.
     * </p>
     */
    public List<Prestamo> listarPrestamosPorUsuario(int idUsuario) throws SQLException {
        return prestamoDAO.findByUsuario(idUsuario);
    }

    public int registrarPrestamo(int idUsuario, int idLibro, LocalDate fechaPrestamo, LocalDate fechaDevolucion) throws SQLException {
        if (fechaPrestamo == null || fechaDevolucion == null) {
            throw new IllegalArgumentException("Las fechas del préstamo son obligatorias.");
        }
        if (fechaDevolucion.isBefore(fechaPrestamo)) {
            throw new IllegalArgumentException("La fecha de devolución no puede ser menor a la fecha de préstamo.");
        }
        return prestamoDAO.registrarPrestamo(idUsuario, idLibro, fechaPrestamo, fechaDevolucion);
    }

    public boolean registrarDevolucion(int idPrestamo) throws SQLException {
        return prestamoDAO.registrarDevolucion(idPrestamo);
    }

    public boolean eliminarPrestamo(int idPrestamo) throws SQLException {
        return prestamoDAO.delete(idPrestamo);
    }
}
