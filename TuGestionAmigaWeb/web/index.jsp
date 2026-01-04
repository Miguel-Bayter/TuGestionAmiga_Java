<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    // Página de entrada del módulo web.
    // Para simplificar la navegación, redirigimos directamente al servlet de login.
    response.sendRedirect(request.getContextPath() + "/login");
%>
