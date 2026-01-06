package com.tugestionamiga.spring.model;

/**
 * Modelo (POJO) que representa un registro de la tabla {@code usuario}.
 *
 * <p>
 * Este objeto se usa para transportar datos entre repositorios y controladores sin exponer SQL
 * en la capa web.
 * </p>
 *
 * <p>
 * El campo {@code correo} es importante porque Spring Security lo utiliza como identificador del
 * usuario autenticado (username). El campo {@code idRol} se usa para asociar el usuario con la
 * tabla {@code rol} y definir permisos (ADMIN/USUARIO).
 * </p>
 */
public class Usuario {

    private int idUsuario;
    private String nombre;
    private String correo;
    private String contrasena;
    private Integer idRol;

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
}
