<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Tu Gestión Amiga - Iniciar sesión</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css" rel="stylesheet">
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/tga.css">
    </head>
    <body class="tga-auth">
        <div class="container py-5">
            <div class="tga-auth-card">
                <div class="card tga-card">
                    <div class="card-body p-4 p-md-5">
                        <div class="d-flex align-items-center gap-2 mb-3">
                            <div class="rounded-3 bg-primary bg-opacity-10 p-2">
                                <i class="bi bi-book-half text-primary fs-4"></i>
                            </div>
                            <div>
                                <div class="tga-auth-brand">Tu Gestión Amiga</div>
                                <div class="text-muted">Acceso al módulo web</div>
                            </div>
                        </div>

                        <h1 class="h4 mb-4">Iniciar sesión</h1>

            <%
                String mensaje = (String) session.getAttribute("mensaje");
                // El servlet puede enviar un mensaje de error si las credenciales no son válidas.
                String error = (String) request.getAttribute("error");
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
                }
            %>

                        <form action="<%=request.getContextPath()%>/login" method="post" class="row g-3">
                            <div class="col-12">
                                <label class="form-label">Correo</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-envelope"></i></span>
                                    <input type="email" class="form-control" name="correo" required>
                                </div>
                            </div>

                            <div class="col-12">
                                <label class="form-label">Contraseña</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-lock"></i></span>
                                    <input type="password" class="form-control" name="contrasena" required>
                                </div>
                            </div>

                            <div class="col-12 d-grid">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-box-arrow-in-right me-1"></i> Entrar
                                </button>
                            </div>
                        </form>

                        <div class="mt-4 tga-form-hint">
                            Nota: este login valida contra la tabla <code>usuario</code> (campos <code>correo</code> y <code>contraseña</code>).
                        </div>

                        <div class="mt-3 tga-form-hint">
                            ¿No tienes cuenta?
                            <a href="<%=request.getContextPath()%>/register" class="link-primary">Regístrate aquí</a>
                        </div>
                    </div>
                </div>
        </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
