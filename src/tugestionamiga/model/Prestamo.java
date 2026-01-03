package tugestionamiga.model;

import java.time.LocalDate;

/**
 * Representa un préstamo de un libro a un usuario.
 *
 * Se mapea con la tabla {@code prestamo}.
 *
 * Campos importantes:
 * - {@code fechaPrestamo}: día en que se entrega el libro.
 * - {@code fechaDevolucion}: día esperado de devolución.
 * - {@code estado}: cadena de estado (por ejemplo: ACTIVO, DEVUELTO).
 */
public class Prestamo {

    /** Identificador del préstamo (PK). */
    private int idPrestamo;
    /** Fecha en la que se registró el préstamo. */
    private LocalDate fechaPrestamo;
    /** Fecha esperada para la devolución. */
    private LocalDate fechaDevolucion;
    /** Estado del préstamo (ej.: ACTIVO, DEVUELTO). */
    private String estado;
    /** Usuario asociado al préstamo (FK a {@code usuario}). */
    private Integer idUsuario;
    /** Libro asociado al préstamo (FK a {@code libro}). */
    private Integer idLibro;

    /** Constructor vacío. */
    public Prestamo() {
    }

    /**
     * Constructor con datos.
     *
     * @param idPrestamo id del préstamo
     * @param fechaPrestamo fecha de préstamo
     * @param fechaDevolucion fecha de devolución
     * @param estado estado
     * @param idUsuario id del usuario (puede ser null)
     * @param idLibro id del libro (puede ser null)
     */
    public Prestamo(int idPrestamo, LocalDate fechaPrestamo, LocalDate fechaDevolucion, String estado, Integer idUsuario, Integer idLibro) {
        this.idPrestamo = idPrestamo;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
        this.idUsuario = idUsuario;
        this.idLibro = idLibro;
    }

    /**
     * Obtiene el id del préstamo.
     *
     * @return id del préstamo
     */
    public int getIdPrestamo() {
        return idPrestamo;
    }

    /**
     * Establece el id del préstamo.
     *
     * @param idPrestamo id del préstamo
     */
    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    /**
     * Obtiene la fecha de préstamo.
     *
     * @return fecha de préstamo
     */
    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    /**
     * Establece la fecha de préstamo.
     *
     * @param fechaPrestamo fecha de préstamo
     */
    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    /**
     * Obtiene la fecha de devolución.
     *
     * @return fecha de devolución
     */
    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    /**
     * Establece la fecha de devolución.
     *
     * @param fechaDevolucion fecha de devolución
     */
    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    /**
     * Obtiene el estado del préstamo.
     *
     * @return estado
     */
    public String getEstado() {
        return estado;
    }

    /**
     * Establece el estado del préstamo.
     *
     * @param estado estado
     */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /**
     * Obtiene el id del usuario asociado.
     *
     * @return id del usuario o null si no aplica
     */
    public Integer getIdUsuario() {
        return idUsuario;
    }

    /**
     * Establece el id del usuario asociado.
     *
     * @param idUsuario id del usuario (puede ser null)
     */
    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Obtiene el id del libro asociado.
     *
     * @return id del libro o null si no aplica
     */
    public Integer getIdLibro() {
        return idLibro;
    }

    /**
     * Establece el id del libro asociado.
     *
     * @param idLibro id del libro (puede ser null)
     */
    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    @Override
    public String toString() {
        return "Prestamo{" + "idPrestamo=" + idPrestamo + ", fechaPrestamo=" + fechaPrestamo + ", fechaDevolucion=" + fechaDevolucion + ", estado=" + estado + ", idUsuario=" + idUsuario + ", idLibro=" + idLibro + '}';
    }
}
