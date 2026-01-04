<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="model.Usuario"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Tu Gestión Amiga - Dashboard</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/tga.css">
    </head>
    <body>

        <%
            // Se obtiene el usuario desde la sesión (lo establece LoginServlet).
            Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
            boolean esAdmin = (u != null && u.esAdministrador());
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
                        <span><%= (u != null) ? u.getNombre() : "" %></span>
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
                                <a class="nav-link active" href="<%=request.getContextPath()%>/dashboard"><i class="bi bi-house-door"></i>Inicio</a>
                                <% if (esAdmin) { %>
                                <a class="nav-link" href="<%=request.getContextPath()%>/usuarios?accion=listar"><i class="bi bi-people"></i>Usuarios</a>
                                <% } %>
                                <a class="nav-link" href="<%=request.getContextPath()%>/libros?accion=listar"><i class="bi bi-journal-bookmark"></i>Libros</a>
                                <a class="nav-link" href="<%=request.getContextPath()%>/prestamos"><i class="bi bi-arrow-left-right"></i>Préstamos</a>
                            </nav>
                        </div>
                    </div>
                </div>

                <main class="col-lg-10 tga-content">
                    <div class="tga-page-header">
                        <div>
                            <h1>Dashboard</h1>
                            <p class="tga-subtitle">Panel principal del módulo web</p>
                        </div>
                    </div>

                    <div class="card tga-card mb-3">
                        <div class="card-body">
                            <h2 class="h5 mb-2">Bienvenido</h2>
                            <div class="text-muted">
                                <% if (u != null) { %>
                                    Has iniciado sesión como <strong><%= u.getNombre() %></strong> (<%= u.getCorreo() %>).
                                <% } else { %>
                                    Sesión no encontrada.
                                <% } %>
                            </div>
                        </div>
                    </div>

                    <div class="row g-3">
                        <% if (esAdmin) { %>
                        <div class="col-md-6 col-xl-4">
                            <a class="card tga-card tga-hover text-decoration-none h-100" href="<%=request.getContextPath()%>/usuarios?accion=listar">
                                <div class="card-body">
                                    <div class="d-flex align-items-center gap-2 mb-2">
                                        <i class="bi bi-people text-primary fs-4"></i>
                                        <h3 class="h6 m-0">Usuarios</h3>
                                    </div>
                                    <div class="text-muted">Crear, listar, editar y eliminar usuarios.</div>
                                </div>
                            </a>
                        </div>
                        <% } %>

                        <div class="col-md-6 col-xl-4">
                            <a class="card tga-card tga-hover text-decoration-none h-100" href="<%=request.getContextPath()%>/libros?accion=listar">
                                <div class="card-body">
                                    <div class="d-flex align-items-center gap-2 mb-2">
                                        <i class="bi bi-journal-bookmark text-primary fs-4"></i>
                                        <h3 class="h6 m-0">Libros</h3>
                                    </div>
                                    <div class="text-muted">Administrar catálogo y disponibilidad.</div>
                                </div>
                            </a>
                        </div>

                        <div class="col-md-6 col-xl-4">
                            <a class="card tga-card tga-hover text-decoration-none h-100" href="<%=request.getContextPath()%>/prestamos">
                                <div class="card-body">
                                    <div class="d-flex align-items-center gap-2 mb-2">
                                        <i class="bi bi-arrow-left-right text-primary fs-4"></i>
                                        <h3 class="h6 m-0">Préstamos</h3>
                                    </div>
                                    <div class="text-muted">Registrar préstamos y devoluciones.</div>
                                </div>
                            </a>
                        </div>
                    </div>
                </main>
            </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
        <script src="<%=request.getContextPath()%>/js/script.js"></script>
    </body>
</html>
