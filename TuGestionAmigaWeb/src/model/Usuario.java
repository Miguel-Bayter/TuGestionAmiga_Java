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

    @Override
    public String toString() {
        return "Usuario{" + "idUsuario=" + idUsuario + ", nombre=" + nombre + ", correo=" + correo + ", idRol=" + idRol + '}';
    }
}
