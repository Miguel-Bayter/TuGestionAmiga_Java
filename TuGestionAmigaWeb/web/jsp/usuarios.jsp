<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Usuario"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Usuarios</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/tga.css">
    </head>
    <body>
        <%
            Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
        %>

        <nav class="navbar navbar-expand-lg navbar-dark tga-navbar">
            <div class="container-fluid">
                <a class="navbar-brand" href="<%=request.getContextPath()%>/dashboard">
                    <i class="bi bi-book-half me-2"></i>Tu Gestión Amiga
                </a>

                <button class="navbar-toggler" type="button" data-bs-toggle="offcanvas" data-bs-target="#tgaSidebar" aria-controls="tgaSidebar">
                    <span class="navbar-toggler-icon"></span>
                </button>

                <div class="ms-auto d-flex align-items-center gap-3 text-white small">
                    <div class="d-none d-md-flex align-items-center gap-2">
                        <i class="bi bi-person-circle"></i>
                        <span><%= (usuarioSesion != null) ? usuarioSesion.getNombre() : "" %></span>
                    </div>
                    <a class="btn btn-light btn-sm" href="<%=request.getContextPath()%>/logout">
                        <i class="bi bi-box-arrow-right me-1"></i>Cerrar sesión
                    </a>
                </div>
            </div>
        </nav>

        <div class="container-fluid tga-layout">
            <div class="row g-0">
                <div class="col-lg-2">
                    <div class="offcanvas-lg offcanvas-start tga-sidebar" tabindex="-1" id="tgaSidebar">
                        <div class="offcanvas-header d-lg-none">
                            <h5 class="offcanvas-title">Menú</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="offcanvas" aria-label="Close"></button>
                        </div>
                        <div class="offcanvas-body p-0">
                            <div class="tga-sidebar-header">
                                <p class="tga-sidebar-title">Navegación</p>
                            </div>
                            <nav class="nav flex-column">
                                <a class="nav-link" href="<%=request.getContextPath()%>/dashboard"><i class="bi bi-house-door"></i>Inicio</a>
                                <a class="nav-link active" href="<%=request.getContextPath()%>/usuarios?accion=listar"><i class="bi bi-people"></i>Usuarios</a>
                                <a class="nav-link" href="<%=request.getContextPath()%>/libros?accion=listar"><i class="bi bi-journal-bookmark"></i>Libros</a>
                                <a class="nav-link" href="<%=request.getContextPath()%>/prestamos"><i class="bi bi-arrow-left-right"></i>Préstamos</a>
                            </nav>
                        </div>
                    </div>
                </div>

                <main class="col-lg-10 tga-content">
                    <div class="tga-page-header">
                        <div>
                            <h1>Usuarios</h1>
                            <p class="tga-subtitle">CRUD de la tabla <code>usuario</code></p>
                        </div>
                    </div>

            <%
                // Mensajes almacenados en sesión por el servlet (después se limpian para no repetir).
                String mensaje = (String) session.getAttribute("mensaje");
                String error = (String) session.getAttribute("error");
                if (mensaje != null) {
            %>
            <div class="alert alert-success"><%= mensaje %></div>
            <%
                    session.removeAttribute("mensaje");
                }
                if (error != null) {
            %>
            <div class="alert alert-danger"><%= error %></div>
            <%
                    session.removeAttribute("error");
                }
            %>

            <%
                Usuario usuarioEdit = (Usuario) request.getAttribute("usuarioEdit");
                boolean editando = (usuarioEdit != null && usuarioEdit.getIdUsuario() > 0);
            %>

                    <div class="row g-3">
                        <div class="col-xl-4">
                            <div class="card tga-card">
                                <div class="card-header">
                                    <strong><%= editando ? "Editar usuario" : "Registrar usuario" %></strong>
                                </div>
                                <div class="card-body">
                                    <form action="<%=request.getContextPath()%>/usuarios" method="post" class="row g-3">
                                        <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">

                                        <% if (editando) { %>
                                        <input type="hidden" name="id" value="<%= usuarioEdit.getIdUsuario() %>">
                                        <% } %>

                                        <div class="col-12">
                                            <label class="form-label">Nombre</label>
                                            <input type="text" class="form-control" name="nombre" required value="<%= editando ? usuarioEdit.getNombre() : "" %>">
                                        </div>

                                        <div class="col-12">
                                            <label class="form-label">Correo</label>
                                            <input type="email" class="form-control" name="correo" required value="<%= editando ? usuarioEdit.getCorreo() : "" %>">
                                        </div>

                                        <div class="col-12">
                                            <label class="form-label">Contraseña</label>
                                            <input type="password" class="form-control" name="contrasena" required value="<%= editando ? usuarioEdit.getContrasena() : "" %>">
                                        </div>

                                        <div class="col-12">
                                            <label class="form-label">ID Rol (opcional)</label>
                                            <input type="number" class="form-control" name="idRol" value="<%= (editando && usuarioEdit.getIdRol() != null) ? usuarioEdit.getIdRol() : "" %>">
                                        </div>

                                        <div class="col-12 d-grid">
                                            <button type="submit" class="btn btn-primary">
                                                <i class="bi bi-save me-1"></i><%= editando ? "Actualizar" : "Registrar" %>
                                            </button>
                                        </div>

                                        <% if (editando) { %>
                                        <div class="col-12 d-grid">
                                            <a class="btn btn-outline-secondary" href="<%=request.getContextPath()%>/usuarios?accion=listar">
                                                <i class="bi bi-x-circle me-1"></i>Cancelar edición
                                            </a>
                                        </div>
                                        <% } %>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <div class="col-xl-8">
                            <div class="card tga-card">
                                <div class="card-header">
                                    <strong>Listado</strong>
                                </div>
                                <div class="card-body">
                                    <div class="table-responsive tga-table">
                                        <table class="table table-hover align-middle mb-0">
                                            <thead class="table-light">
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
                            <td>
                                <div class="d-flex gap-2">
                                    <a class="btn btn-sm btn-outline-primary" href="<%=request.getContextPath()%>/usuarios?accion=editar&id=<%=u.getIdUsuario()%>">
                                        <i class="bi bi-pencil me-1"></i>Editar
                                    </a>

                                    <% if (usuarioSesion != null && usuarioSesion.getIdUsuario() == u.getIdUsuario()) { %>
                                        <button type="button" class="btn btn-sm btn-outline-secondary" disabled title="No puedes eliminar tu propio usuario">
                                            <i class="bi bi-shield-lock me-1"></i>Eliminar
                                        </button>
                                    <% } else { %>
                                        <form action="<%=request.getContextPath()%>/usuarios" method="post" class="d-inline">
                                            <input type="hidden" name="accion" value="eliminar">
                                            <input type="hidden" name="id" value="<%= u.getIdUsuario() %>">
                                            <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('¿Eliminar usuario?');">
                                                <i class="bi bi-trash me-1"></i>Eliminar
                                            </button>
                                        </form>
                                    <% } %>
                                </div>
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
                            </div>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script src="<%=request.getContextPath()%>/js/script.js"></script>
    </body>
</html>
