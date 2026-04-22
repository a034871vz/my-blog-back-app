package yandex.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import yandex.practicum.MyBlogBackAppBootApplication;
import yandex.practicum.dto.CommentRequest;
import yandex.practicum.entity.Comment;
import yandex.practicum.entity.Post;
import yandex.practicum.repository.CommentRepository;
import yandex.practicum.repository.PostRepository;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = MyBlogBackAppBootApplication.class)
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class CommentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();
    }

    private String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    void getComments_shouldReturnListOfComments() throws Exception {
        Post post = createPost("Post for comments", "Text", "[]");
        Comment comment1 = createComment(post, "First comment");
        Comment comment2 = createComment(post, "Second comment");

        mockMvc.perform(get("/api/posts/{postId}/comments", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(comment1.getId()))
                .andExpect(jsonPath("$[0].text").value("First comment"))
                .andExpect(jsonPath("$[0].postId").value(post.getId()))
                .andExpect(jsonPath("$[1].id").value(comment2.getId()))
                .andExpect(jsonPath("$[1].text").value("Second comment"))
                .andExpect(jsonPath("$[1].postId").value(post.getId()));
    }

    @Test
    void getComments_shouldReturnEmptyListWhenNoComments() throws Exception {
        Post post = createPost("Post without comments", "Text", "[]");

        mockMvc.perform(get("/api/posts/{postId}/comments", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getComment_shouldReturnComment() throws Exception {
        Post post = createPost("Post", "Text", "[]");
        Comment comment = createComment(post, "Test comment");

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", post.getId(), comment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.text").value("Test comment"))
                .andExpect(jsonPath("$.postId").value(post.getId()));
    }

    @Test
    void getComment_shouldReturn404WhenCommentNotFound() throws Exception {
        Post post = createPost("Post", "Text", "[]");
        mockMvc.perform(get("/api/posts/{postId}/comments/999", post.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getComment_shouldReturn404WhenCommentBelongsToDifferentPost() throws Exception {
        Post post1 = createPost("Post 1", "Text", "[]");
        Post post2 = createPost("Post 2", "Text", "[]");
        Comment comment = createComment(post1, "Comment for post1");

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", post2.getId(), comment.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void createComment_shouldCreateAndReturnComment() throws Exception {
        Post post = createPost("Post for comment", "Text", "[]");
        CommentRequest request = new CommentRequest("New comment");
        String json = asJson(request);

        mockMvc.perform(post("/api/posts/{postId}/comments", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("New comment"))
                .andExpect(jsonPath("$.postId").value(post.getId()));
    }

    @Test
    void createComment_shouldReturn404WhenPostNotFound() throws Exception {
        CommentRequest request = new CommentRequest("New comment");
        String json = asJson(request);

        mockMvc.perform(post("/api/posts/999/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateComment_shouldUpdateAndReturnUpdatedComment() throws Exception {
        Post post = createPost("Post", "Text", "[]");
        Comment comment = createComment(post, "Old text");

        CommentRequest updateRequest = new CommentRequest("Updated text");
        String json = asJson(updateRequest);

        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", post.getId(), comment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comment.getId()))
                .andExpect(jsonPath("$.text").value("Updated text"))
                .andExpect(jsonPath("$.postId").value(post.getId()));
    }

    @Test
    void updateComment_shouldReturn404WhenCommentNotFound() throws Exception {
        Post post = createPost("Post", "Text", "[]");
        CommentRequest request = new CommentRequest("Updated text");
        String json = asJson(request);

        mockMvc.perform(put("/api/posts/{postId}/comments/999", post.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_shouldDeleteCommentAndReturnOk() throws Exception {
        Post post = createPost("Post", "Text", "[]");
        Comment comment = createComment(post, "To delete");

        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", post.getId(), comment.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", post.getId(), comment.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment_shouldReturn404WhenCommentNotFound() throws Exception {
        Post post = createPost("Post", "Text", "[]");
        mockMvc.perform(delete("/api/posts/{postId}/comments/999", post.getId()))
                .andExpect(status().isNotFound());
    }

    private Post createPost(String title, String text, String tags) {
        Post post = new Post();
        post.setTitle(title);
        post.setText(text);
        post.setTags(tags);
        post.setLikesCount(0);
        post.setCommentsCount(0);
        return postRepository.save(post);
    }

    private Comment createComment(Post post, String text) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setPost(post);
        post.setCommentsCount(post.getCommentsCount() + 1);
        postRepository.save(post);
        return commentRepository.save(comment);
    }
}