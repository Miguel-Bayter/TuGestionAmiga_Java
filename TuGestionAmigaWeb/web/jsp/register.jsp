<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Tu Gestión Amiga - Registrarse</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css">
    </head>
    <body>
        <div class="container auth">
            <div class="card">
                <div class="auth-header">
                    <div class="auth-title">Tu Gestión Amiga</div>
                    <h1>Registrarse</h1>
                </div>

            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
            <div class="alert alert-error"><%= error %></div>
            <%
                }
            %>

            <form action="<%=request.getContextPath()%>/register" method="post" class="form">
                <label>Nombre</label>
                <input type="text" name="nombre" required>

                <label>Correo</label>
                <input type="email" name="correo" required>

                <label>Contraseña</label>
                <input type="password" name="contrasena" required>

                <label>Repetir contraseña</label>
                <input type="password" name="contrasena2" required>

                <button type="submit">Crear cuenta</button>
            </form>

            <p class="hint">
                ¿Ya tienes cuenta?
                <a href="<%=request.getContextPath()%>/login">Inicia sesión aquí</a>
            </p>
            </div>
        </div>
    </body>
</html>
