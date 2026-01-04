<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Usuario"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Tu Gestión Amiga - Dashboard</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css">
    </head>
    <body>
        <div class="container">
            <div class="topbar">
                <div>
                    <h1>Dashboard</h1>
                    <p class="muted">Panel principal del módulo web</p>
                </div>
                <div>
                    <a class="btn" href="<%=request.getContextPath()%>/logout">Cerrar sesión</a>
                </div>
            </div>

            <%
                // Se obtiene el usuario desde la sesión (lo establece LoginServlet).
                Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
            %>

            <div class="card">
                <h2>Bienvenido</h2>
                <p>
                    <% if (u != null) { %>
                        Has iniciado sesión como <strong><%= u.getNombre() %></strong> (<%= u.getCorreo() %>).
                    <% } else { %>
                        Sesión no encontrada.
                    <% } %>
                </p>
            </div>

            <div class="grid">
                <a class="card link" href="<%=request.getContextPath()%>/usuarios?accion=listar">
                    <h3>Usuarios</h3>
                    <p>Crear, listar, editar y eliminar usuarios.</p>
                </a>

                <a class="card link" href="<%=request.getContextPath()%>/libros?accion=listar">
                    <h3>Libros</h3>
                    <p>Administrar catálogo y disponibilidad.</p>
                </a>

                <a class="card link" href="<%=request.getContextPath()%>/prestamos">
                    <h3>Préstamos</h3>
                    <p>Registrar préstamos y devoluciones.</p>
                </a>
            </div>
        </div>

        <script src="<%=request.getContextPath()%>/js/script.js"></script>
    </body>
</html>
