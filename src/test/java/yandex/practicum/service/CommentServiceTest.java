package yandex.practicum.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yandex.practicum.dto.CommentRequest;
import yandex.practicum.dto.CommentResponse;
import yandex.practicum.entity.Comment;
import yandex.practicum.entity.Post;
import yandex.practicum.repository.CommentRepository;
import yandex.practicum.repository.PostRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    void getCommentsByPostId_shouldReturnCommentResponses() {
        Long postId = 1L;
        Post post = new Post();
        post.setId(postId);

        Comment comment1 = new Comment();
        comment1.setId(10L);
        comment1.setText("Comment 1");
        comment1.setPost(post);

        Comment comment2 = new Comment();
        comment2.setId(20L);
        comment2.setText("Comment 2");
        comment2.setPost(post);

        when(commentRepository.findByPostId(postId)).thenReturn(List.of(comment1, comment2));

        List<CommentResponse> responses = commentService.getCommentsByPostId(postId);

        assertEquals(2, responses.size());
        assertEquals(10L, responses.getFirst().id());
        assertEquals("Comment 1", responses.get(0).text());
        assertEquals(postId, responses.get(0).postId());
        assertEquals(20L, responses.get(1).id());
        assertEquals("Comment 2", responses.get(1).text());
        assertEquals(postId, responses.get(1).postId());

        verify(commentRepository).findByPostId(postId);
    }

    @Test
    void getComment_shouldReturnCommentResponseWhenExists() {
        Long postId = 1L;
        Long commentId = 10L;
        Post post = new Post();
        post.setId(postId);
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setText("Some text");
        comment.setPost(post);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        CommentResponse response = commentService.getComment(postId, commentId);

        assertEquals(commentId, response.id());
        assertEquals("Some text", response.text());
        assertEquals(postId, response.postId());
        verify(commentRepository).findById(commentId);
    }

    @Test
    void getComment_shouldThrowWhenCommentNotFound() {
        Long postId = 1L;
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> commentService.getComment(postId, commentId));
        verify(commentRepository).findById(commentId);
    }

    @Test
    void getComment_shouldThrowWhenCommentBelongsToDifferentPost() {
        Long postId = 1L;
        Long commentId = 10L;
        Post otherPost = new Post();
        otherPost.setId(999L);
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setPost(otherPost);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        
        assertThrows(RuntimeException.class, () -> commentService.getComment(postId, commentId));
        verify(commentRepository).findById(commentId);
    }

    @Test
    void createComment_shouldSaveAndReturnResponse() {
        Long postId = 1L;
        String text = "New comment";
        Post post = new Post();
        post.setId(postId);
        post.setCommentsCount(0);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        Comment savedComment = new Comment();
        savedComment.setId(100L);
        savedComment.setText(text);
        savedComment.setPost(post);

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        
        CommentResponse response = commentService.createComment(postId, new CommentRequest(text));

        assertEquals(100L, response.id());
        assertEquals(text, response.text());
        assertEquals(postId, response.postId());
        
        verify(postRepository).save(post);
        assertEquals(1, post.getCommentsCount());
        
        verify(commentRepository).save(argThat(comment ->
                comment.getText().equals(text) && comment.getPost().equals(post)
        ));
    }

    @Test
    void createComment_shouldThrowWhenPostNotFound() {
        Long postId = 999L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> commentService.createComment(postId, new CommentRequest("Text")));
        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void updateComment_shouldUpdateAndReturnResponse() {
        Long postId = 1L;
        Long commentId = 10L;
        String newText = "Updated comment";
        Post post = new Post();
        post.setId(postId);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setText("Old text");
        comment.setPost(post);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));
        when(commentRepository.save(comment)).thenReturn(comment);
        
        CommentResponse response = commentService.updateComment(postId, commentId, new CommentRequest(newText));
        
        assertEquals(commentId, response.id());
        assertEquals(newText, response.text());
        assertEquals(postId, response.postId());

        assertEquals(newText, comment.getText());
        verify(commentRepository).save(comment);
    }

    @Test
    void updateComment_shouldThrowWhenCommentNotFound() {
        Long postId = 1L;
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> commentService.updateComment(postId, commentId, new CommentRequest("New text")));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void updateComment_shouldThrowWhenCommentBelongsToDifferentPost() {
        Long postId = 1L;
        Long commentId = 10L;
        Post otherPost = new Post();
        otherPost.setId(999L);
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setPost(otherPost);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(RuntimeException.class, () -> commentService.updateComment(postId, commentId, new CommentRequest("New text")));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void deleteComment_shouldDeleteAndDecrementCommentsCount() {
        Long postId = 1L;
        Long commentId = 10L;
        Post post = new Post();
        post.setId(postId);
        post.setCommentsCount(2);

        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setPost(post);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        commentService.deleteComment(postId, commentId);

        assertEquals(1, post.getCommentsCount());
        verify(postRepository).save(post);
        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_shouldThrowWhenCommentNotFound() {
        Long postId = 1L;
        Long commentId = 999L;
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> commentService.deleteComment(postId, commentId));
        verify(commentRepository, never()).delete(any());
        verify(postRepository, never()).save(any());
    }

    @Test
    void deleteComment_shouldThrowWhenCommentBelongsToDifferentPost() {
        Long postId = 1L;
        Long commentId = 10L;
        Post otherPost = new Post();
        otherPost.setId(999L);
        Comment comment = new Comment();
        comment.setId(commentId);
        comment.setPost(otherPost);

        when(commentRepository.findById(commentId)).thenReturn(Optional.of(comment));

        assertThrows(RuntimeException.class, () -> commentService.deleteComment(postId, commentId));
        verify(commentRepository, never()).delete(any());
        verify(postRepository, never()).save(any());
    }
}