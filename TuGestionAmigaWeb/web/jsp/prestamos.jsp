<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Usuario"%>
<%@page import="model.Libro"%>
<%@page import="model.Prestamo"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Préstamos</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css">
    </head>
    <body>
        <div class="container">
            <div class="topbar">
                <div>
                    <h1>Préstamos</h1>
                    <p class="muted">Registrar préstamo y devolución</p>
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

                List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
                List<Libro> libros = (List<Libro>) request.getAttribute("libros");
                List<Prestamo> prestamos = (List<Prestamo>) request.getAttribute("prestamos");
            %>

            <div class="grid">
                <div class="card">
                    <h2>Registrar préstamo</h2>

                    <form action="<%=request.getContextPath()%>/prestamos" method="post" class="form">
                        <input type="hidden" name="accion" value="registrarPrestamo">

                        <label>Usuario</label>
                        <select name="idUsuario" required>
                            <option value="">Seleccione...</option>
                            <%
                                if (usuarios != null) {
                                    for (Usuario u : usuarios) {
                            %>
                            <option value="<%=u.getIdUsuario()%>"><%=u.getIdUsuario()%> - <%=u.getNombre()%> (<%=u.getCorreo()%>)</option>
                            <%
                                    }
                                }
                            %>
                        </select>

                        <label>Libro</label>
                        <select name="idLibro" required>
                            <option value="">Seleccione...</option>
                            <%
                                if (libros != null) {
                                    for (Libro l : libros) {
                            %>
                            <option value="<%=l.getIdLibro()%>"><%=l.getIdLibro()%> - <%=l.getTitulo()%> (<%= l.isDisponible() ? "Disponible" : "No disponible" %>)</option>
                            <%
                                    }
                                }
                            %>
                        </select>

                        <label>Fecha préstamo</label>
                        <input type="date" name="fechaPrestamo" required>

                        <label>Fecha devolución</label>
                        <input type="date" name="fechaDevolucion" required>

                        <button type="submit">Registrar</button>
                    </form>
                </div>

                <div class="card">
                    <h2>Registrar devolución</h2>

                    <form action="<%=request.getContextPath()%>/prestamos" method="post" class="form">
                        <input type="hidden" name="accion" value="registrarDevolucion">

                        <label>ID Préstamo</label>
                        <input type="number" name="idPrestamo" required>

                        <button type="submit">Devolver</button>
                    </form>

                    <p class="hint">
                        La devolución cambia el estado del préstamo a <code>DEVUELTO</code> y marca el libro como disponible.
                    </p>
                </div>
            </div>

            <div class="card">
                <h2>Listado de préstamos</h2>

                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Fecha préstamo</th>
                            <th>Fecha devolución</th>
                            <th>Estado</th>
                            <th>ID Usuario</th>
                            <th>ID Libro</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            if (prestamos != null) {
                                for (Prestamo p : prestamos) {
                        %>
                        <tr>
                            <td><%= p.getIdPrestamo() %></td>
                            <td><%= p.getFechaPrestamo() %></td>
                            <td><%= p.getFechaDevolucion() %></td>
                            <td><%= p.getEstado() %></td>
                            <td><%= (p.getIdUsuario() != null) ? p.getIdUsuario() : "" %></td>
                            <td><%= (p.getIdLibro() != null) ? p.getIdLibro() : "" %></td>
                            <td class="actions">
                                <form action="<%=request.getContextPath()%>/prestamos" method="post" class="inline">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="<%= p.getIdPrestamo() %>">
                                    <button type="submit" class="btn btn-small btn-danger" onclick="return confirm('¿Eliminar préstamo?');">Eliminar</button>
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
