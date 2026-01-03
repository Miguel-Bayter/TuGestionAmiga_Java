package tugestionamiga.model;

/**
 * Representa un usuario del sistema.
 *
 * Se mapea con la tabla {@code usuario} de la base de datos.
 *
 * Campos principales:
 * - {@code idUsuario}: identificador autogenerado.
 * - {@code nombre}: nombre visible.
 * - {@code correo}: correo único.
 * - {@code contrasena}: contraseña almacenada (según el esquema actual).
 * - {@code idRol}: referencia opcional a la tabla {@code rol}.
 */
public class Usuario {

    /** Identificador del usuario (PK). */
    private int idUsuario;
    /** Nombre del usuario. */
    private String nombre;
    /** Correo del usuario (único). */
    private String correo;
    /** Contraseña del usuario. */
    private String contrasena;
    /** Rol del usuario (FK opcional). */
    private Integer idRol;

    /**
     * Constructor vacío.
     * Se usa cuando se crea el objeto y se cargan datos con setters.
     */
    public Usuario() {
    }

    /**
     * Constructor con datos.
     *
     * @param idUsuario id del usuario
     * @param nombre nombre
     * @param correo correo
     * @param contrasena contraseña
     * @param idRol id del rol (puede ser null)
     */
    public Usuario(int idUsuario, String nombre, String correo, String contrasena, Integer idRol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.idRol = idRol;
    }

    /**
     * Obtiene el identificador del usuario.
     *
     * @return id del usuario
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * Establece el identificador del usuario.
     *
     * @param idUsuario id del usuario
     */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return nombre
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Establece el nombre del usuario.
     *
     * @param nombre nombre
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /**
     * Obtiene el correo del usuario.
     *
     * @return correo
     */
    public String getCorreo() {
        return correo;
    }

    /**
     * Establece el correo del usuario.
     *
     * @param correo correo
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }

    /**
     * Obtiene la contraseña del usuario.
     *
     * @return contraseña
     */
    public String getContrasena() {
        return contrasena;
    }

    /**
     * Establece la contraseña del usuario.
     *
     * @param contrasena contraseña
     */
    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    /**
     * Obtiene el id del rol asociado.
     *
     * @return id del rol o null si no aplica
     */
    public Integer getIdRol() {
        return idRol;
    }

    /**
     * Establece el id del rol asociado.
     *
     * @param idRol id del rol (puede ser null)
     */
    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    @Override
    public String toString() {
        return "Usuario{" + "idUsuario=" + idUsuario + ", nombre=" + nombre + ", correo=" + correo + ", idRol=" + idRol + '}';
    }
}
