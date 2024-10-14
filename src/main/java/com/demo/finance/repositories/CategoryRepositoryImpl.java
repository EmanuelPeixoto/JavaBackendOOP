package com.demo.finance.repositories;

import com.demo.finance.exceptions.FinanceBadRequestException;
import com.demo.finance.exceptions.FinanceResourceNotFoundException;
import com.demo.finance.models.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private static final String SQL_CREATE =
            "INSERT INTO categories (category_id, user_id, title, description) VALUES (NEXTVAL('cat_seq'), ?, ?, ?)";

    private static final String SQL_FIND_BY_ID = "SELECT c.category_id, c.user_id, c.title, c.description, " +
            "COALESCE(SUM(t.amount), 0) total_expense " +
            "FROM transactions t RIGHT OUTER JOIN categories c ON c.category_id = t.category_id " +
            "WHERE c.user_id = ? AND c.category_id = ? GROUP BY c.category_id ";

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Override
    public List<Category> findAll(Integer user_id) throws FinanceResourceNotFoundException {
        return List.of();
    }

    @Override
    public Category findById(Integer user_id, Integer category_id) throws FinanceResourceNotFoundException {
        try {
            return jdbcTemplate.queryForObject(SQL_FIND_BY_ID, new Object[]{user_id, category_id}, categoryRowMapper);
        } catch (Exception e) {
            throw new FinanceResourceNotFoundException("Category not found");
        }
    }

    @Override
    public Integer create(Integer user_id, String title, String description) throws FinanceBadRequestException {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(SQL_CREATE, PreparedStatement.RETURN_GENERATED_KEYS);
                ps.setInt(1, user_id);
                ps.setString(2, title);
                ps.setString(3, description);
                return ps;
            }, keyHolder);
            return (Integer) keyHolder.getKeys().get("category_id");
        } catch (Exception e) {
            throw new FinanceBadRequestException("Invalid request");
        }
    }

    @Override
    public void update(Integer user_id, Integer category_id, Category category) throws FinanceBadRequestException {

    }

    @Override
    public void deleteById(Integer user_id, Integer category_id) {

    }

    private RowMapper<Category> categoryRowMapper = ((rs, rowNum) -> {
        return new Category(rs.getInt("category_id"),
            rs.getInt("user_id"),
            rs.getString("title"),
            rs.getString("description"),
            rs.getDouble("total_expense")
        );
    });
}