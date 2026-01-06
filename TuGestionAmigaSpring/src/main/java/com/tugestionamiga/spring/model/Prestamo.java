package com.tugestionamiga.spring.model;

import java.time.LocalDate;

/**
 * Modelo (POJO) que representa un préstamo en la tabla {@code prestamo}.
 *
 * <p>
 * Un préstamo registra el movimiento de un libro hacia un usuario, con:
 * </p>
 *
 * <ul>
 *   <li>Fecha de préstamo</li>
 *   <li>Fecha de devolución (compromiso/fecha objetivo)</li>
 *   <li>Estado: normalmente {@code ACTIVO} o {@code DEVUELTO}</li>
 *   <li>Relación a usuario y libro</li>
 * </ul>
 *
 * <p>
 * El estado permite distinguir préstamos actuales de los ya devueltos.
 * La actualización de stock al prestar/devolver se maneja en el repositorio.
 * </p>
 */
public class Prestamo {

    private int idPrestamo;
    private LocalDate fechaPrestamo;
    private LocalDate fechaDevolucion;
    private String estado;
    private Integer idUsuario;
    private Integer idLibro;

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
}
