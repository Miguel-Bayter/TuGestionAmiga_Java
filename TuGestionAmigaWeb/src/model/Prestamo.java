package model;

import java.time.LocalDate;

/**
 * Representa un préstamo de un libro a un usuario.
 *
 * <p>
 * Esta clase se usa para mostrar y registrar préstamos desde el módulo web.
 * </p>
 */
public class Prestamo {

    /** Identificador del préstamo (PK). */
    private int idPrestamo;
    /** Fecha en la que se registra el préstamo. */
    private LocalDate fechaPrestamo;
    /** Fecha esperada de devolución. */
    private LocalDate fechaDevolucion;
    /** Estado del préstamo (por ejemplo: ACTIVO, DEVUELTO). */
    private String estado;
    /** Usuario asociado (FK). */
    private Integer idUsuario;
    /** Libro asociado (FK). */
    private Integer idLibro;

    /** Constructor vacío. */
    public Prestamo() {
    }

    /** Constructor con datos. */
    public Prestamo(int idPrestamo, LocalDate fechaPrestamo, LocalDate fechaDevolucion, String estado, Integer idUsuario, Integer idLibro) {
        this.idPrestamo = idPrestamo;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
        this.estado = estado;
        this.idUsuario = idUsuario;
        this.idLibro = idLibro;
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public LocalDate getFechaPrestamo() {
        return fechaPrestamo;
    }

    public void setFechaPrestamo(LocalDate fechaPrestamo) {
        this.fechaPrestamo = fechaPrestamo;
    }

    public LocalDate getFechaDevolucion() {
        return fechaDevolucion;
    }

    public void setFechaDevolucion(LocalDate fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    @Override
    public String toString() {
        return "Prestamo{" + "idPrestamo=" + idPrestamo + ", fechaPrestamo=" + fechaPrestamo + ", fechaDevolucion=" + fechaDevolucion + ", estado=" + estado + ", idUsuario=" + idUsuario + ", idLibro=" + idLibro + '}';
    }
}
