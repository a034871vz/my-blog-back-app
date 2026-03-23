package yandex.practicum.repository;

import yandex.practicum.model.User;
import java.util.List;

public interface UserRepository {
    List<User> findAll();

    void save(User user);

    void deleteById(Long id);

    void update(Long id, User user);
}