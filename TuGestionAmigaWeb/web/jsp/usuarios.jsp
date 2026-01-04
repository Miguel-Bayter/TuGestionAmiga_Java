<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Usuario"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Usuarios</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css">
    </head>
    <body>
        <div class="container">
            <div class="topbar">
                <div>
                    <h1>Usuarios</h1>
                    <p class="muted">CRUD de la tabla <code>usuario</code></p>
                </div>
                <div class="topbar-actions">
                    <a class="btn" href="<%=request.getContextPath()%>/dashboard">Volver</a>
                    <a class="btn" href="<%=request.getContextPath()%>/logout">Cerrar sesión</a>
                </div>
            </div>

            <%
                // Mensajes almacenados en sesión por el servlet (después se limpian para no repetir).
                String mensaje = (String) session.getAttribute("mensaje");
                String error = (String) session.getAttribute("error");
                if (mensaje != null) {
            %>
            <div class="alert alert-ok"><%= mensaje %></div>
            <%
                    session.removeAttribute("mensaje");
                }
                if (error != null) {
            %>
            <div class="alert alert-error"><%= error %></div>
            <%
                    session.removeAttribute("error");
                }
            %>

            <%
                Usuario usuarioEdit = (Usuario) request.getAttribute("usuarioEdit");
                boolean editando = (usuarioEdit != null && usuarioEdit.getIdUsuario() > 0);
            %>

            <div class="card">
                <h2><%= editando ? "Editar usuario" : "Registrar usuario" %></h2>

                <form action="<%=request.getContextPath()%>/usuarios" method="post" class="form">
                    <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">

                    <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= usuarioEdit.getIdUsuario() %>">
                    <% } %>

                    <label>Nombre</label>
                    <input type="text" name="nombre" required value="<%= editando ? usuarioEdit.getNombre() : "" %>">

                    <label>Correo</label>
                    <input type="email" name="correo" required value="<%= editando ? usuarioEdit.getCorreo() : "" %>">

                    <label>Contraseña</label>
                    <input type="password" name="contrasena" required value="<%= editando ? usuarioEdit.getContrasena() : "" %>">

                    <label>ID Rol (opcional)</label>
                    <input type="number" name="idRol" value="<%= (editando && usuarioEdit.getIdRol() != null) ? usuarioEdit.getIdRol() : "" %>">

                    <button type="submit"><%= editando ? "Actualizar" : "Registrar" %></button>
                </form>
            </div>

            <div class="card">
                <h2>Listado</h2>

                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Nombre</th>
                            <th>Correo</th>
                            <th>ID Rol</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
                            if (usuarios != null) {
                                for (Usuario u : usuarios) {
                        %>
                        <tr>
                            <td><%= u.getIdUsuario() %></td>
                            <td><%= u.getNombre() %></td>
                            <td><%= u.getCorreo() %></td>
                            <td><%= (u.getIdRol() != null) ? u.getIdRol() : "" %></td>
                            <td class="actions">
                                <a class="btn btn-small" href="<%=request.getContextPath()%>/usuarios?accion=editar&id=<%=u.getIdUsuario()%>">Editar</a>

                                <form action="<%=request.getContextPath()%>/usuarios" method="post" class="inline">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="<%= u.getIdUsuario() %>">
                                    <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('¿Eliminar usuario?');">Eliminar</button>
                                </form>
                            </td>
                        </tr>
                        <%
                                }
                            }
                        %>
                    </tbody>
                </table>
            </div>
        </div>

        <script src="<%=request.getContextPath()%>/js/script.js"></script>
    </body>
</html>
