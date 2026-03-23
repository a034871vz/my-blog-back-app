package yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yandex.practicum.dto.CommentRequest;
import yandex.practicum.dto.CommentResponse;
import yandex.practicum.entity.Comment;
import yandex.practicum.entity.Post;
import yandex.practicum.repository.CommentRepository;
import yandex.practicum.repository.PostRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId).stream()
                .map(CommentResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CommentResponse getComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> c.getPost().getId().equals(postId))
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        return CommentResponse.from(comment);
    }

    @Transactional
    public CommentResponse createComment(Long postId, CommentRequest request) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Пост не найден"));
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
        Comment saved = commentRepository.save(new Comment(request, post));
        return CommentResponse.from(saved);
    }

    @Transactional
    public CommentResponse updateComment(Long postId, Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> c.getPost().getId().equals(postId))
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        comment.setText(request.text());
        Comment updated = commentRepository.save(comment);
        return CommentResponse.from(updated);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .filter(c -> c.getPost().getId().equals(postId))
                .orElseThrow(() -> new RuntimeException("Комментарий не найден"));
        Post post = comment.getPost();
        post.setCommentsCount(post.getCommentsCount() - 1);
        postRepository.save(post);
        commentRepository.delete(comment);
    }
}