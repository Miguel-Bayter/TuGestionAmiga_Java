package com.tugestionamiga.spring.model;

/**
 * Modelo (POJO) para la tabla {@code categoria}.
 *
 * <p>
 * Se usa para clasificar libros por género/categoría y poder mostrar un selector amigable en la UI.
 * </p>
 */
public class Categoria {

    private int idCategoria;
    private String nombreCategoria;

    public int getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }
}
