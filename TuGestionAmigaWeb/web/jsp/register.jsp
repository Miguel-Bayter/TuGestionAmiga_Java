<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Tu Gestión Amiga - Registrarse</title>
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
                                <i class="bi bi-person-plus text-primary fs-4"></i>
                            </div>
                            <div>
                                <div class="tga-auth-brand">Tu Gestión Amiga</div>
                                <div class="text-muted">Crear cuenta</div>
                            </div>
                        </div>

                        <h1 class="h4 mb-4">Registrarse</h1>

            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
            <div class="alert alert-danger"><%= error %></div>
            <%
                }
            %>

                        <form action="<%=request.getContextPath()%>/register" method="post" class="row g-3">
                            <div class="col-12">
                                <label class="form-label">Nombre</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-person"></i></span>
                                    <input type="text" class="form-control" name="nombre" required>
                                </div>
                            </div>

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

                            <div class="col-12">
                                <label class="form-label">Confirmar contraseña</label>
                                <div class="input-group">
                                    <span class="input-group-text"><i class="bi bi-shield-lock"></i></span>
                                    <input type="password" class="form-control" name="contrasena2" required>
                                </div>
                            </div>

                            <div class="col-12 d-grid">
                                <button type="submit" class="btn btn-primary">
                                    <i class="bi bi-person-check me-1"></i> Crear cuenta
                                </button>
                            </div>
                        </form>

                        <div class="mt-3 tga-form-hint">
                            ¿Ya tienes cuenta?
                            <a href="<%=request.getContextPath()%>/login" class="link-primary">Inicia sesión aquí</a>
                        </div>
                    </div>
                </div>
        </div>
        </div>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>
</html>
