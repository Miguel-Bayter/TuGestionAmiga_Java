package com.tugestionamiga.spring.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filtro que refresca las autoridades (roles) desde la base de datos en cada request.
 *
 * <p>
 * Esto permite que cuando un administrador cambie el rol de un usuario (ADMIN/USUARIO),
 * el cambio se refleje inmediatamente sin que el usuario tenga que cerrar sesión.
 * </p>
 */
public class RefreshAuthoritiesFilter extends OncePerRequestFilter {

    private final JdbcTemplate jdbcTemplate;

    public RefreshAuthoritiesFilter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && auth.getName() != null
                && !(auth instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {

            String correo = auth.getName();

            List<String> authoritiesFromDb = jdbcTemplate.query(
                    "SELECT COALESCE(CONCAT('ROLE_', r.nombre_rol), 'ROLE_USUARIO') AS authority "
                    + "FROM usuario u LEFT JOIN rol r ON u.id_rol = r.id_rol WHERE u.correo = ?",
                    (rs, rowNum) -> rs.getString("authority"),
                    correo
            );

            if (authoritiesFromDb == null || authoritiesFromDb.isEmpty()) {
                SecurityContextHolder.clearContext();
            } else {
                List<GrantedAuthority> refreshed = authoritiesFromDb.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                boolean changed = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet())
                        .equals(refreshed.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet())) == false;

                if (changed) {
                    User principal = new User(correo, "", refreshed);
                    UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
                            principal,
                            auth.getCredentials(),
                            refreshed
                    );
                    newAuth.setDetails(auth.getDetails());
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
