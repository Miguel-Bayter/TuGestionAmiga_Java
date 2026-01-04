<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Tu Gestión Amiga - Iniciar sesión</title>
        <link rel="stylesheet" href="<%=request.getContextPath()%>/css/styles.css">
    </head>
    <body>
        <div class="container auth">
            <div class="card">
                <div class="auth-header">
                    <div class="auth-title">Tu Gestión Amiga</div>
                    <h1>Iniciar sesión</h1>
                </div>

            <%
                String mensaje = (String) session.getAttribute("mensaje");
                // El servlet puede enviar un mensaje de error si las credenciales no son válidas.
                String error = (String) request.getAttribute("error");
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
                }
            %>

            <form action="<%=request.getContextPath()%>/login" method="post" class="form">
                <label>Correo</label>
                <input type="email" name="correo" required>

                <label>Contraseña</label>
                <input type="password" name="contrasena" required>

                <button type="submit">Entrar</button>
            </form>

            <p class="hint">
                Nota: este login valida contra la tabla <code>usuario</code> (campos <code>correo</code> y <code>contraseña</code>).
            </p>

            <p class="hint">
                ¿No tienes cuenta?
                <a href="<%=request.getContextPath()%>/register">Regístrate aquí</a>
            </p>
            </div>
        </div>
    </body>
</html>
