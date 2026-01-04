package model;

/**
 * Representa un usuario del sistema.
 *
 * <p>
 * Esta clase se usa en el módulo web para transportar datos entre:
 * </p>
 *
 * <ul>
 *   <li>El DAO (cuando se leen registros desde MySQL).</li>
 *   <li>Los Servlets (cuando se reciben/validan datos de formularios).</li>
 *   <li>Las JSP (cuando se muestra información dinámica).</li>
 * </ul>
 */
public class Usuario {

    /**
     * Identificador esperado del rol ADMIN.
     *
     * <p>
     * En el esquema existe la tabla {@code rol}. Para que esta convención funcione, se recomienda
     * tener un registro con {@code id_rol=1} para ADMIN.
     * </p>
     */
    public static final int ROL_ADMIN = 1;

    /**
     * Identificador esperado del rol USUARIO.
     *
     * <p>
     * Se propone usar {@code id_rol=2} para el rol normal.
     * </p>
     */
    public static final int ROL_USUARIO = 2;

    /** Identificador del usuario (PK). */
    private int idUsuario;
    /** Nombre del usuario. */
    private String nombre;
    /** Correo único del usuario. */
    private String correo;
    /** Contraseña del usuario (según el esquema actual). */
    private String contrasena;
    /** Rol asociado (FK opcional). */
    private Integer idRol;

    /** Constructor vacío. */
    public Usuario() {
    }

    /**
     * Constructor con datos.
     */
    public Usuario(int idUsuario, String nombre, String correo, String contrasena, Integer idRol) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.idRol = idRol;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    /**
     * Indica si el usuario actual se considera administrador.
     */
    public boolean esAdministrador() {
        return idRol != null && idRol == ROL_ADMIN;
    }

    /**
     * Indica si el usuario actual se considera usuario normal.
     *
     * <p>
     * Si {@code idRol} es {@code null}, se toma como usuario normal (para no romper registros antiguos).
     * </p>
     */
    public boolean esUsuario() {
        return idRol == null || idRol == ROL_USUARIO;
    }

    @Override
    public String toString() {
        return "Usuario{" + "idUsuario=" + idUsuario + ", nombre=" + nombre + ", correo=" + correo + ", idRol=" + idRol + '}';
    }
}
