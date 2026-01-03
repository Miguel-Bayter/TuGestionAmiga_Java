package tugestionamiga.model;

/**
 * Representa un rol de usuario.
 *
 * Se mapea con la tabla {@code rol}. En la tabla {@code usuario}, el campo
 * {@code id_rol} es opcional, por lo que un usuario puede no tener rol asignado.
 */
public class Rol {

    /** Identificador del rol (PK). */
    private int idRol;
    /** Nombre del rol. */
    private String nombreRol;

    /** Constructor vacío. */
    public Rol() {
    }

    /**
     * Constructor con datos.
     *
     * @param idRol id del rol
     * @param nombreRol nombre del rol
     */
    public Rol(int idRol, String nombreRol) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
    }

    /**
     * Obtiene el id del rol.
     *
     * @return id del rol
     */
    public int getIdRol() {
        return idRol;
    }

    /**
     * Establece el id del rol.
     *
     * @param idRol id del rol
     */
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombreRol() {
        return nombreRol;
    }

    /**
     * Establece el nombre del rol.
     *
     * @param nombreRol nombre del rol
     */
    public void setNombreRol(String nombreRol) {
        this.nombreRol = nombreRol;
    }

    @Override
    public String toString() {
        return "Rol{" + "idRol=" + idRol + ", nombreRol=" + nombreRol + '}';
    }
}
