package yandex.practicum.service;

import org.springframework.stereotype.Service;
import yandex.practicum.model.User;
import yandex.practicum.repository.UserRepository;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public void update(Long id, User user) {
        userRepository.update(id, user);
    }
}