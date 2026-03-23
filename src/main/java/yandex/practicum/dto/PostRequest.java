package yandex.practicum.dto;

import java.util.List;

public record PostRequest(
        String title,
        String text,
        List<String> tags
) {
}