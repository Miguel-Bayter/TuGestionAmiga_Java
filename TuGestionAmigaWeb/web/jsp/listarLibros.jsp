<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Libro"%>
<%@page import="model.Usuario"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Libros</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/tga.css">
    </head>
    <body>
        <%
            Usuario usuarioSesion = (Usuario) session.getAttribute("usuarioLogueado");
            boolean esAdmin = (usuarioSesion != null && usuarioSesion.esAdministrador());
        %>
        <nav class="navbar navbar-expand-lg navbar-dark tga-navbar">
            <div class="container-fluid">
                <a class="navbar-brand" href="<%=request.getContextPath()%>/dashboard">
                    <i class="bi bi-book-half me-2"></i>Tu Gestión Amiga
                </a>

                <button class="navbar-toggler" type="button" data-bs-toggle="offcanvas" data-bs-target="#tgaSidebar" aria-controls="tgaSidebar">
                    <span class="navbar-toggler-icon"></span>
                </button>

                <a class="ms-auto btn btn-light btn-sm" href="<%=request.getContextPath()%>/logout">
                    <i class="bi bi-box-arrow-right me-1"></i>Cerrar sesión
                </a>
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
                                <% if (esAdmin) { %>
                                <a class="nav-link" href="<%=request.getContextPath()%>/usuarios?accion=listar"><i class="bi bi-people"></i>Usuarios</a>
                                <% } %>
                                <a class="nav-link active" href="<%=request.getContextPath()%>/libros?accion=listar"><i class="bi bi-journal-bookmark"></i>Libros</a>
                                <a class="nav-link" href="<%=request.getContextPath()%>/prestamos"><i class="bi bi-arrow-left-right"></i>Préstamos</a>
                            </nav>
                        </div>
                    </div>
                </div>

                <main class="col-lg-10 tga-content">
                    <div class="tga-page-header">
                        <div>
                            <h1>Libros</h1>
                            <p class="tga-subtitle">CRUD de la tabla <code>libro</code></p>
                        </div>
                    </div>

            <%
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
                Libro libroEdit = (Libro) request.getAttribute("libroEdit");
                boolean editando = (libroEdit != null && libroEdit.getIdLibro() > 0);
            %>

                    <div class="row g-3">
                        <% if (esAdmin) { %>
                        <div class="col-xl-4">
                            <div class="card tga-card">
                                <div class="card-header">
                                    <strong><%= editando ? "Editar libro" : "Registrar libro" %></strong>
                                </div>
                                <div class="card-body">
                                    <form action="<%=request.getContextPath()%>/libros" method="post" class="row g-3">
                                        <input type="hidden" name="accion" value="<%= editando ? "actualizar" : "crear" %>">

                                        <% if (editando) { %>
                                        <input type="hidden" name="id" value="<%= libroEdit.getIdLibro() %>">
                                        <% } %>

                                        <div class="col-12">
                                            <label class="form-label">Título</label>
                                            <input type="text" class="form-control" name="titulo" required value="<%= editando ? libroEdit.getTitulo() : "" %>">
                                        </div>

                                        <div class="col-12">
                                            <label class="form-label">Autor</label>
                                            <input type="text" class="form-control" name="autor" required value="<%= editando ? libroEdit.getAutor() : "" %>">
                                        </div>

                                        <div class="col-12">
                                            <label class="form-label">Descripción</label>
                                            <textarea class="form-control" name="descripcion" rows="3"><%= editando ? (libroEdit.getDescripcion() != null ? libroEdit.getDescripcion() : "") : "" %></textarea>
                                        </div>

                                        <div class="col-12">
                                            <label class="form-label">Disponible</label>
                                            <select class="form-select" name="disponible">
                                                <option value="1" <%= (editando && libroEdit.isDisponible()) ? "selected" : "" %>>Sí</option>
                                                <option value="0" <%= (editando && !libroEdit.isDisponible()) ? "selected" : "" %>>No</option>
                                            </select>
                                        </div>

                                        <div class="col-12">
                                            <label class="form-label">ID Categoría (opcional)</label>
                                            <input type="number" class="form-control" name="idCategoria" value="<%= (editando && libroEdit.getIdCategoria() != null) ? libroEdit.getIdCategoria() : "" %>">
                                        </div>

                                        <div class="col-12 d-grid">
                                            <button type="submit" class="btn btn-primary">
                                                <i class="bi bi-save me-1"></i><%= editando ? "Actualizar" : "Registrar" %>
                                            </button>
                                        </div>

                                        <% if (editando) { %>
                                        <div class="col-12 d-grid">
                                            <a class="btn btn-outline-secondary" href="<%=request.getContextPath()%>/libros?accion=listar">
                                                <i class="bi bi-x-circle me-1"></i>Cancelar edición
                                            </a>
                                        </div>
                                        <% } %>
                                    </form>
                                </div>
                            </div>
                        </div>
                        <% } %>

                        <div class="<%= esAdmin ? "col-xl-8" : "col-12" %>">
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
                                                    <th>Título</th>
                                                    <th>Autor</th>
                                                    <th>Disponible</th>
                                                    <th>ID Categoría</th>
                                                    <% if (esAdmin) { %><th>Acciones</th><% } %>
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
                            <td>
                                <% if (l.isDisponible()) { %>
                                    <span class="badge text-bg-success">Disponible</span>
                                <% } else { %>
                                    <span class="badge text-bg-secondary">No disponible</span>
                                <% } %>
                            </td>
                            <td><%= (l.getIdCategoria() != null) ? l.getIdCategoria() : "" %></td>
                            <% if (esAdmin) { %>
                            <td>
                                <div class="d-flex gap-2">
                                    <a class="btn btn-sm btn-outline-primary" href="<%=request.getContextPath()%>/libros?accion=editar&id=<%=l.getIdLibro()%>">
                                        <i class="bi bi-pencil me-1"></i>Editar
                                    </a>

                                <form action="<%=request.getContextPath()%>/libros" method="post" class="d-inline">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="<%= l.getIdLibro() %>">
                                    <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('¿Eliminar libro?');">
                                        <i class="bi bi-trash me-1"></i>Eliminar
                                    </button>
                                </form>
                                </div>
                            </td>
                            <% } %>
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
