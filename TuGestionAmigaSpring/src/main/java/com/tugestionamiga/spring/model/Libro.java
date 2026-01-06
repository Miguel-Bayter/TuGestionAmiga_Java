package com.tugestionamiga.spring.model;

 import java.math.BigDecimal;

/**
 * Modelo (POJO) que representa un registro de la tabla {@code libro}.
 *
 * <p>
 * En esta versión se añadió el atributo {@code stock} para controlar inventario.
 * La aplicación utiliza la regla:
 * </p>
 *
 * <pre>
 * disponible = (stock &gt; 0)
 * </pre>
 */
public class Libro {

    private int idLibro;
    private String titulo;
    private String autor;
    private String descripcion;
    private boolean disponible;
    private int stock;

    /**
     * Valor (precio unitario) del libro.
     *
     * <p>
     * Se usa principalmente en el módulo de compras para calcular el total:
     * </p>
     *
     * <pre>
     * total = valor * cantidad
     * </pre>
     */
    private BigDecimal valor;
    private Integer idCategoria;
    private String nombreCategoria;

    public int getIdLibro() {
        return idLibro;
    }

    public void setIdLibro(int idLibro) {
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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }
}
