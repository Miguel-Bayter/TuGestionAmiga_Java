package model;

/**
 * Representa una categoría (género) de libros.
 *
 * <p>
 * Se corresponde con la tabla {@code categoria} y se usa para poblar el selector de
 * “Género / Categoría” al crear/editar un libro.
 * </p>
 */
public class Categoria {

    private int idCategoria;
    private String nombreCategoria;

    public Categoria() {
    }

    public Categoria(int idCategoria, String nombreCategoria) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

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
