package yandex.practicum.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import yandex.practicum.model.User;

import java.util.List;

@Repository
public class JdbcNativeUserRepository implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(
                "select id, first_name, last_name, age, active from users",
                (rs, rowNum) -> new User(
                        rs.getLong("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age"),
                        rs.getBoolean("active")
                ));
    }

    @Override
    public void save(User user) {
        // Формируем insert-запрос с параметрами
        jdbcTemplate.update("insert into users(first_name, last_name, age, active) values(?, ?, ?, ?)",
                user.getFirstName(), user.getLastName(), user.getAge(), user.isActive());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("delete from users where id = ?", id);
    }

    @Override
    public void update(Long id, User user) {
        jdbcTemplate.update("update users set first_name = ?, last_name = ?, age = ?, active = ? where id = ?",
                user.getFirstName(), user.getLastName(), user.getAge(), user.isActive(), id);
    }

}