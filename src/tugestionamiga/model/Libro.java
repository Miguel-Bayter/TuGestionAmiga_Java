package tugestionamiga.model;

/**
 * Representa un libro dentro del catálogo.
 *
 * Se mapea con la tabla {@code libro} de la base de datos.
 *
 * El campo {@code disponible} corresponde a {@code disponibilidad} en la BD:
 * - {@code true}  : el libro se puede prestar.
 * - {@code false} : el libro está prestado/no disponible.
 */
public class Libro {

    /** Identificador del libro (PK). */
    private int idLibro;
    /** Título del libro. */
    private String titulo;
    /** Autor del libro. */
    private String autor;
    /** Descripción del libro (puede ser null). */
    private String descripcion;
    /** Indica si el libro está disponible para préstamo. */
    private boolean disponible;
    /** Categoría del libro (FK opcional a {@code categoria}). */
    private Integer idCategoria;

    /** Constructor vacío. */
    public Libro() {
    }

    /**
     * Constructor con datos.
     *
     * @param idLibro id del libro
     * @param titulo título
     * @param autor autor
     * @param descripcion descripción
     * @param disponible disponibilidad actual
     * @param idCategoria id de categoría (puede ser null)
     */
    public Libro(int idLibro, String titulo, String autor, String descripcion, boolean disponible, Integer idCategoria) {
        this.idLibro = idLibro;
        this.titulo = titulo;
        this.autor = autor;
        this.descripcion = descripcion;
        this.disponible = disponible;
        this.idCategoria = idCategoria;
    }

    /**
     * Obtiene el identificador del libro.
     *
     * @return id del libro
     */
    public int getIdLibro() {
        return idLibro;
    }

    /**
     * Establece el identificador del libro.
     *
     * @param idLibro id del libro
     */
    public void setIdLibro(int idLibro) {
        this.idLibro = idLibro;
    }

    /**
     * Obtiene el título del libro.
     *
     * @return título
     */
    public String getTitulo() {
        return titulo;
    }

    /**
     * Establece el título del libro.
     *
     * @param titulo título
     */
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    /**
     * Obtiene el autor del libro.
     *
     * @return autor
     */
    public String getAutor() {
        return autor;
    }

    /**
     * Establece el autor del libro.
     *
     * @param autor autor
     */
    public void setAutor(String autor) {
        this.autor = autor;
    }

    /**
     * Obtiene la descripción del libro.
     *
     * @return descripción (puede ser null)
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción del libro.
     *
     * @param descripcion descripción
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /**
     * Indica si el libro está disponible para préstamo.
     *
     * @return true si está disponible
     */
    public boolean isDisponible() {
        return disponible;
    }

    /**
     * Cambia la disponibilidad del libro.
     *
     * @param disponible true si se desea marcar como disponible
     */
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    /**
     * Obtiene el id de la categoría.
     *
     * @return id de categoría o null si no aplica
     */
    public Integer getIdCategoria() {
        return idCategoria;
    }

    /**
     * Establece el id de la categoría.
     *
     * @param idCategoria id de categoría (puede ser null)
     */
    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    @Override
    public String toString() {
        return "Libro{" + "idLibro=" + idLibro + ", titulo=" + titulo + ", autor=" + autor + ", disponible=" + disponible + ", idCategoria=" + idCategoria + '}';
    }
}
