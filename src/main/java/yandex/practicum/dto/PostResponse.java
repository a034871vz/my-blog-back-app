package yandex.practicum.dto;

import lombok.NoArgsConstructor;
import yandex.practicum.entity.Post;

import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String text,
        List<String> tags,
        Integer likesCount,
        Integer commentsCount
) {

    public static PostResponse from(Post post) {
        String truncatedText = post.getText();
        if (truncatedText != null && truncatedText.length() > 128) {
            truncatedText = truncatedText.substring(0, 128) + "…";
        }
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                truncatedText,
                post.getTags(),
                post.getLikesCount(),
                post.getCommentsCount()
        );
    }
}