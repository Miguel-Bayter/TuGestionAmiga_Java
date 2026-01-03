package tugestionamiga.model;

/**
 * Representa una categoría de libros.
 *
 * Se mapea con la tabla {@code categoria}. En este proyecto la categoría es
 * opcional para un libro, por eso en {@code Libro} el campo {@code idCategoria}
 * puede ser null.
 */
public class Categoria {

    /** Identificador de la categoría (PK). */
    private int idCategoria;
    /** Nombre de la categoría. */
    private String nombreCategoria;

    /** Constructor vacío. */
    public Categoria() {
    }

    /**
     * Constructor con datos.
     *
     * @param idCategoria id de categoría
     * @param nombreCategoria nombre
     */
    public Categoria(int idCategoria, String nombreCategoria) {
        this.idCategoria = idCategoria;
        this.nombreCategoria = nombreCategoria;
    }

    /**
     * Obtiene el id de la categoría.
     *
     * @return id de categoría
     */
    public int getIdCategoria() {
        return idCategoria;
    }

    /**
     * Establece el id de la categoría.
     *
     * @param idCategoria id de categoría
     */
    public void setIdCategoria(int idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    /**
     * Establece el nombre de la categoría.
     *
     * @param nombreCategoria nombre
     */
    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    @Override
    public String toString() {
        return "Categoria{" + "idCategoria=" + idCategoria + ", nombreCategoria=" + nombreCategoria + '}';
    }
}
