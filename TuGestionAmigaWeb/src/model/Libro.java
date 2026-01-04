package model;

/**
 * Representa un libro dentro del catálogo.
 *
 * <p>
 * El atributo {@code disponible} corresponde a la columna {@code disponibilidad} en la base de datos.
 * </p>
 */
public class Libro {

    /** Identificador del libro (PK). */
    private int idLibro;
    /** Título del libro. */
    private String titulo;
    /** Autor del libro. */
    private String autor;
    /** Descripción (puede ser null). */
    private String descripcion;
    /** Indica si el libro está disponible para préstamo. */
    private boolean disponible;
    /** Categoría del libro (FK opcional). */
    private Integer idCategoria;

    /** Constructor vacío. */
    public Libro() {
    }

    /** Constructor con datos. */
    public Libro(int idLibro, String titulo, String autor, String descripcion, boolean disponible, Integer idCategoria) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.disponible = disponible;
        this.idCategoria = idCategoria;
    }

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

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    @Override
    public String toString() {
        return "Libro{" + "idLibro=" + idLibro + ", titulo=" + titulo + ", autor=" + autor + ", disponible=" + disponible + ", idCategoria=" + idCategoria + '}';
    }
}
