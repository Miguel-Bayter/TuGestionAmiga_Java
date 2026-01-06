package com.tugestionamiga.spring.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO para mostrar compras en la pantalla de perfil.
 *
 * <p>
 * Este objeto existe separado de {@link Compra} porque la vista de perfil necesita datos extra del
 * libro (título/autor) obtenidos con un {@code JOIN}.
 * En lugar de hacer varias consultas o sobrecargar el modelo base, se usa este DTO para mapear
 * directamente el resultado de la consulta.
 * </p>
 */
public class CompraPerfilRow {

    private int idCompra;
    private LocalDate fechaCompra;
    private BigDecimal precio;
    private Integer idLibro;
    private String titulo;
    private String autor;

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

    public Integer getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(Integer idLibro) {
        this.idLibro = idLibro;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }
}
