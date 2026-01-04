<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Libro"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Libros</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css">
    </head>
    <body>
        <div class="container">
            <div class="topbar">
                <div>
                    <h1>Libros</h1>
                    <p class="muted">CRUD de la tabla <code>libro</code></p>
                </div>
                <div class="topbar-actions">
                    <a class="btn" href="<%=request.getContextPath()%>/dashboard">Volver</a>
                    <a class="btn" href="<%=request.getContextPath()%>/logout">Cerrar sesión</a>
                </div>
            </div>

            <%
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
                Libro libroEdit = (Libro) request.getAttribute("libroEdit");
                boolean editando = (libroEdit != null && libroEdit.getIdLibro() > 0);
            %>

            <div class="card">
                <h2><%= editando ? "Editar libro" : "Registrar libro" %></h2>

                <form action="<%=request.getContextPath()%>/libros" method="post" class="form">
                    <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">

                    <% if (editando) { %>
                    <input type="hidden" name="id" value="<%= libroEdit.getIdLibro() %>">
                    <% } %>

                    <label>Título</label>
                    <input type="text" name="titulo" required value="<%= editando ? libroEdit.getTitulo() : "" %>">

                    <label>Autor</label>
                    <input type="text" name="autor" required value="<%= editando ? libroEdit.getAutor() : "" %>">

                    <label>Descripción</label>
                    <textarea name="descripcion" rows="3"><%= editando ? (libroEdit.getDescripcion() != null ? libroEdit.getDescripcion() : "") : "" %></textarea>

                    <label>Disponible</label>
                    <select name="disponible">
                        <option value="1" <%= (editando && libroEdit.isDisponible()) ? "selected" : "" %>>Sí</option>
                        <option value="0" <%= (editando && !libroEdit.isDisponible()) ? "selected" : "" %>>No</option>
                    </select>

                    <label>ID Categoría (opcional)</label>
                    <input type="number" name="idCategoria" value="<%= (editando && libroEdit.getIdCategoria() != null) ? libroEdit.getIdCategoria() : "" %>">

                    <button type="submit"><%= editando ? "Actualizar" : "Registrar" %></button>
                </form>
            </div>

            <div class="card">
                <h2>Listado</h2>

                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Título</th>
                            <th>Autor</th>
                            <th>Disponible</th>
                            <th>ID Categoría</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            List<Libro> libros = (List<Libro>) request.getAttribute("libros");
                            if (libros != null) {
                                for (Libro l : libros) {
                        %>
                        <tr>
                            <td><%= l.getIdLibro() %></td>
                            <td><%= l.getTitulo() %></td>
                            <td><%= l.getAutor() %></td>
                            <td><%= l.isDisponible() ? "Sí" : "No" %></td>
                            <td><%= (l.getIdCategoria() != null) ? l.getIdCategoria() : "" %></td>
                            <td class="actions">
                                <a class="btn btn-small" href="<%=request.getContextPath()%>/libros?accion=editar&id=<%=l.getIdLibro()%>">Editar</a>

                                <form action="<%=request.getContextPath()%>/libros" method="post" class="inline">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="<%= l.getIdLibro() %>">
                                    <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('¿Eliminar libro?');">Eliminar</button>
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
