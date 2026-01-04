<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.List"%>
<%@page import="model.Usuario"%>
<%@page import="model.Libro"%>
<%@page import="model.Prestamo"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Préstamos</title>
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
                                <a class="nav-link" href="<%=request.getContextPath()%>/libros?accion=listar"><i class="bi bi-journal-bookmark"></i>Libros</a>
                                <a class="nav-link active" href="<%=request.getContextPath()%>/prestamos"><i class="bi bi-arrow-left-right"></i>Préstamos</a>
                            </nav>
                        </div>
                    </div>
                </div>

                <main class="col-lg-10 tga-content">
                    <div class="tga-page-header">
                        <div>
                            <h1>Préstamos</h1>
                            <p class="tga-subtitle">Registrar préstamo y devolución</p>
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

                List<Usuario> usuarios = (List<Usuario>) request.getAttribute("usuarios");
                List<Libro> libros = (List<Libro>) request.getAttribute("libros");
                List<Prestamo> prestamos = (List<Prestamo>) request.getAttribute("prestamos");
            %>

                    <div class="row g-3">
                        <div class="col-xl-6">
                            <div class="card tga-card">
                                <div class="card-header">
                                    <strong><i class="bi bi-plus-circle me-1"></i>Registrar préstamo</strong>
                                </div>
                                <div class="card-body">
                                    <form action="<%=request.getContextPath()%>/prestamos" method="post" class="row g-3">
                                        <input type="hidden" name="accion" value="registrarPrestamo">

                                        <% if (esAdmin) { %>
                                        <div class="col-12">
                                            <label class="form-label">Usuario</label>
                                            <select class="form-select" name="idUsuario" required>
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
                                        </div>
                                        <% } %>

                                        <div class="col-12">
                                            <label class="form-label">Libro</label>
                                            <select class="form-select" name="idLibro" required>
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
                                        </div>

                                        <div class="col-md-6">
                                            <label class="form-label">Fecha préstamo</label>
                                            <input type="date" class="form-control" name="fechaPrestamo" required>
                                        </div>

                                        <div class="col-md-6">
                                            <label class="form-label">Fecha devolución</label>
                                            <input type="date" class="form-control" name="fechaDevolucion" required>
                                        </div>

                                        <div class="col-12 d-grid">
                                            <button type="submit" class="btn btn-primary">
                                                <i class="bi bi-check2-circle me-1"></i>Registrar
                                            </button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <% if (esAdmin) { %>
                        <div class="col-xl-6">
                            <div class="card tga-card">
                                <div class="card-header">
                                    <strong><i class="bi bi-arrow-return-left me-1"></i>Registrar devolución</strong>
                                </div>
                                <div class="card-body">
                                    <form action="<%=request.getContextPath()%>/prestamos" method="post" class="row g-3">
                                        <input type="hidden" name="accion" value="registrarDevolucion">

                                        <div class="col-12">
                                            <label class="form-label">ID Préstamo</label>
                                            <input type="number" class="form-control" name="idPrestamo" required>
                                        </div>

                                        <div class="col-12 d-grid">
                                            <button type="submit" class="btn btn-outline-primary">
                                                <i class="bi bi-box-arrow-in-left me-1"></i>Devolver
                                            </button>
                                        </div>
                                    </form>

                                    <div class="mt-3 text-muted small">
                                        La devolución cambia el estado del préstamo a <code>DEVUELTO</code> y marca el libro como disponible.
                                    </div>
                                </div>
                            </div>
                        </div>
                        <% } %>
                    </div>

                    <div class="card tga-card mt-3">
                        <div class="card-header">
                            <strong>Listado de préstamos</strong>
                        </div>
                        <div class="card-body">
                            <div class="table-responsive tga-table">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th>ID</th>
                                            <th>Fecha préstamo</th>
                                            <th>Fecha devolución</th>
                                            <th>Estado</th>
                                            <% if (esAdmin) { %><th>ID Usuario</th><% } %>
                                            <th>ID Libro</th>
                                            <% if (esAdmin) { %><th>Acciones</th><% } %>
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
                            <td>
                                <% if (p.getEstado() != null && p.getEstado().equalsIgnoreCase("DEVUELTO")) { %>
                                    <span class="badge text-bg-secondary">DEVUELTO</span>
                                <% } else { %>
                                    <span class="badge text-bg-success">ACTIVO</span>
                                <% } %>
                            </td>
                            <% if (esAdmin) { %><td><%= (p.getIdUsuario() != null) ? p.getIdUsuario() : "" %></td><% } %>
                            <td><%= (p.getIdLibro() != null) ? p.getIdLibro() : "" %></td>
                            <% if (esAdmin) { %>
                            <td>
                                <form action="<%=request.getContextPath()%>/prestamos" method="post" class="d-inline">
                                    <input type="hidden" name="accion" value="eliminar">
                                    <input type="hidden" name="id" value="<%= p.getIdPrestamo() %>">
                                    <button type="submit" class="btn btn-sm btn-danger" onclick="return confirm('¿Eliminar préstamo?');">
                                        <i class="bi bi-trash me-1"></i>Eliminar
                                    </button>
                                </form>
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
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script src="<%=request.getContextPath()%>/js/script.js"></script>
    </body>
</html>
