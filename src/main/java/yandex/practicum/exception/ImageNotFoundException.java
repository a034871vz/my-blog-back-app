package yandex.practicum.exception;

public class ImageNotFoundException extends RuntimeException {
    public ImageNotFoundException(Long id) {
        super("Изображение для поста с id " + id + " не найдено");
    }
}