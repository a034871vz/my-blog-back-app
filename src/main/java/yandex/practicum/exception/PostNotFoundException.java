package yandex.practicum.exception;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long id) {
        super("Пост с id " + id + " не найден");
    }
}