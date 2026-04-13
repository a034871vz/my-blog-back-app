package yandex.practicum.exception;

public class CommentNotFoundException extends RuntimeException {
    public CommentNotFoundException(Long id) {
        super("Комментарий с id " + id + " не найден");
    }
}