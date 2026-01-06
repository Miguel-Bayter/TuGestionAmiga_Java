package com.tugestionamiga.spring.config;

import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;

/**
 * Configuración de seguridad del módulo Spring Boot.
 *
 * <p>
 * La idea es que el login sea totalmente manejado por Spring Security, pero usando la base de datos
 * existente del proyecto:
 * </p>
 *
 * <ul>
 *   <li>La tabla {@code usuario} aporta el correo y la contraseña.</li>
 *   <li>La tabla {@code rol} aporta el nombre del rol para construir autoridades del tipo {@code ROLE_ADMIN}.</li>
 * </ul>
 *
 * <p>
 * Para facilitar la prueba rápida con el esquema actual, la contraseña se trata como texto plano
 * usando el prefijo {@code {noop}} (sin hashing). Más adelante se puede reemplazar por BCrypt.
 * </p>
 *
 * <p>
 * Además, se incluye un filtro ({@link RefreshAuthoritiesFilter}) para refrescar roles desde la BD.
 * Esto existe porque en un módulo de administración es común cambiar el rol de un usuario mientras
 * este tiene sesión activa, y se busca que el cambio se refleje sin obligar a cerrar sesión.
 * </p>
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JdbcTemplate jdbcTemplate) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/usuarios/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(Customizer.withDefaults());

        http.addFilterAfter(new RefreshAuthoritiesFilter(jdbcTemplate), SecurityContextHolderFilter.class);

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(DataSource dataSource) {
        JdbcUserDetailsManager manager = new JdbcUserDetailsManager(dataSource);

        manager.setUsersByUsernameQuery(
                "SELECT correo AS username, CONCAT('{noop}', `contraseña`) AS password, TRUE AS enabled "
                + "FROM usuario WHERE correo = ?"
        );

        manager.setAuthoritiesByUsernameQuery(
                "SELECT u.correo AS username, COALESCE(CONCAT('ROLE_', r.nombre_rol), 'ROLE_USUARIO') AS authority "
                + "FROM usuario u LEFT JOIN rol r ON u.id_rol = r.id_rol WHERE u.correo = ?"
        );

        return manager;
    }
}
