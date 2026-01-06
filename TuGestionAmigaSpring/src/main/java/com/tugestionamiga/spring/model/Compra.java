package com.tugestionamiga.spring.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo (POJO) que representa una compra registrada en la tabla {@code compra}.
 *
 * <p>
 * Existe para almacenar la información mínima de una compra:
 * </p>
 *
 * <ul>
 *   <li>Fecha de compra</li>
 *   <li>Precio</li>
 *   <li>Relación con usuario ({@code idUsuario})</li>
 *   <li>Relación con libro ({@code idLibro})</li>
 * </ul>
 *
 * <p>
 * En el módulo Spring, este modelo se usa para listados y para registrar compras.
 * La lógica de stock no está aquí, sino en el repositorio transaccional.
 * </p>
 */
public class Compra {

    private int idCompra;
    private LocalDate fechaCompra;
    private BigDecimal precio;
    private Integer idUsuario;
    private Integer idLibro;

    public int getIdCompra() {
        return idCompra;
    }

    public void setIdCompra(int idCompra) {
        this.idCompra = idCompra;
    }

    public LocalDate getFechaCompra() {
        return fechaCompra;
    }

    public void setFechaCompra(LocalDate fechaCompra) {
        this.fechaCompra = fechaCompra;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
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
