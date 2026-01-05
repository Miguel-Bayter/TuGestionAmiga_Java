package com.tugestionamiga.spring.repository;

import com.tugestionamiga.spring.model.Categoria;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CategoriaRepository {

    private final JdbcTemplate jdbcTemplate;

    public CategoriaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Categoria> findAll() {
        return jdbcTemplate.query(
                "SELECT id_categoria, nombre_categoria FROM categoria ORDER BY id_categoria",
                (rs, rowNum) -> {
                    Categoria c = new Categoria();
                    c.setIdCategoria(rs.getInt("id_categoria"));
                    c.setNombreCategoria(rs.getString("nombre_categoria"));
                    return c;
                }
        );
    }
}
