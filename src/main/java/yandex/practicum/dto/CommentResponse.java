package yandex.practicum.dto;

import yandex.practicum.entity.Comment;

public record CommentResponse(Long id, String text, Long postId) {

    public static CommentResponse from(Comment comment) {
        return new CommentResponse(comment.getId(), comment.getText(), comment.getPost().getId());
    }
}