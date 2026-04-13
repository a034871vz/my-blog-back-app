package yandex.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import yandex.practicum.configuration.TestJpaConfig;
import yandex.practicum.configuration.WebConfiguration;
import yandex.practicum.dto.PostRequest;
import yandex.practicum.entity.Post;
import yandex.practicum.repository.PostRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitConfig(classes = {TestJpaConfig.class, WebConfiguration.class})
@WebAppConfiguration
@Transactional
@ActiveProfiles("test")
class PostControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private PostRepository postRepository;


    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();
        postRepository.deleteAll();
    }

    private String asJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    @Test
    void getAllPosts_shouldReturnPageWithPosts() throws Exception {
        Post post1 = new Post();
        post1.setTitle("First Post");
        post1.setText("Content 1");
        post1.setTags("[\"tag1\",\"tag2\"]");
        post1.setLikesCount(3);
        post1.setCommentsCount(1);
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Second Post");
        post2.setText("Content 2");
        post2.setTags("[\"tag3\"]");
        post2.setLikesCount(5);
        post2.setCommentsCount(2);
        postRepository.save(post2);

        mockMvc.perform(get("/api/posts")
                        .param("search", "")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(2))
                .andExpect(jsonPath("$.posts[0].id").value(post1.getId()))
                .andExpect(jsonPath("$.posts[0].title").value("First Post"))
                .andExpect(jsonPath("$.posts[0].text").value("Content 1"))
                .andExpect(jsonPath("$.posts[0].tags[0]").value("tag1"))
                .andExpect(jsonPath("$.posts[0].tags[1]").value("tag2"))
                .andExpect(jsonPath("$.posts[0].likesCount").value(3))
                .andExpect(jsonPath("$.posts[0].commentsCount").value(1))
                .andExpect(jsonPath("$.posts[1].id").value(post2.getId()))
                .andExpect(jsonPath("$.posts[1].title").value("Second Post"))
                .andExpect(jsonPath("$.posts[1].text").value("Content 2"))
                .andExpect(jsonPath("$.posts[1].tags[0]").value("tag3"))
                .andExpect(jsonPath("$.posts[1].likesCount").value(5))
                .andExpect(jsonPath("$.posts[1].commentsCount").value(2))
                .andExpect(jsonPath("$.hasPrev").value(false))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.lastPage").value(1));
    }

    @Test
    void getAllPosts_withSearch_shouldFilterPosts() throws Exception {
        Post post1 = new Post();
        post1.setTitle("Java Blog Post");
        post1.setText("About Java");
        post1.setTags("[\"java\"]");
        postRepository.save(post1);

        Post post2 = new Post();
        post2.setTitle("Spring Framework");
        post2.setText("Spring Boot");
        post2.setTags("[\"spring\"]");
        postRepository.save(post2);

        mockMvc.perform(get("/api/posts")
                        .param("search", "Java")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.posts.length()").value(1))
                .andExpect(jsonPath("$.posts[0].title").value("Java Blog Post"));
    }

    @Test
    void getPostById_shouldReturnPost() throws Exception {
        Post post = new Post();
        post.setTitle("Test Post");
        post.setText("Test text");
        post.setTags("[\"test\"]");
        post.setLikesCount(7);
        post.setCommentsCount(0);
        post = postRepository.save(post);

        mockMvc.perform(get("/api/posts/{id}", post.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(post.getId()))
                .andExpect(jsonPath("$.title").value("Test Post"))
                .andExpect(jsonPath("$.text").value("Test text"))
                .andExpect(jsonPath("$.tags[0]").value("test"))
                .andExpect(jsonPath("$.likesCount").value(7))
                .andExpect(jsonPath("$.commentsCount").value(0));
    }

    @Test
    void getPostById_shouldReturn404WhenNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createPost_shouldCreateAndReturnCreatedPost() throws Exception {
        PostRequest request = new PostRequest("New Post", "New text", List.of("tag1", "tag2"));
        String json = asJson(request);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Post"))
                .andExpect(jsonPath("$.text").value("New text"))
                .andExpect(jsonPath("$.tags[0]").value("tag1"))
                .andExpect(jsonPath("$.tags[1]").value("tag2"))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));
    }

    @Test
    void updatePost_shouldUpdateAndReturnUpdatedPost() throws Exception {
        Post existing = new Post();
        existing.setTitle("Old Title");
        existing.setText("Old text");
        existing.setTags("[\"old\"]");
        existing = postRepository.save(existing);

        PostRequest updateRequest = new PostRequest("Updated Title", "Updated text", List.of("new"));
        String json = asJson(updateRequest);

        mockMvc.perform(put("/api/posts/{id}", existing.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(existing.getId()))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.text").value("Updated text"))
                .andExpect(jsonPath("$.tags[0]").value("new"))
                .andExpect(jsonPath("$.likesCount").value(0))
                .andExpect(jsonPath("$.commentsCount").value(0));
    }

    @Test
    void updatePost_shouldReturn404WhenNotFound() throws Exception {
        PostRequest request = new PostRequest("Title", "Text", List.of());
        String json = asJson(request);
        mockMvc.perform(put("/api/posts/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePost_shouldReturnOk() throws Exception {
        Post post = new Post();
        post.setTitle("To delete");
        post.setText("Text");
        post.setTags("[]");
        post = postRepository.save(post);

        mockMvc.perform(delete("/api/posts/{id}", post.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{id}", post.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    void incrementLikes_shouldIncreaseLikesAndReturnNewCount() throws Exception {
        Post post = new Post();
        post.setTitle("Liked Post");
        post.setText("Text");
        post.setTags("[]");
        post.setLikesCount(5);
        post = postRepository.save(post);

        mockMvc.perform(post("/api/posts/{id}/likes", post.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("6"));

        Post updated = postRepository.findById(post.getId()).orElseThrow();
        assertEquals(6, updated.getLikesCount());
    }

    @Test
    void incrementLikes_shouldReturn404WhenPostNotFound() throws Exception {
        mockMvc.perform(post("/api/posts/999/likes"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateImage_shouldUploadImageAndReturnOk() throws Exception {
        Post post = new Post();
        post.setTitle("Image Post");
        post.setText("Text");
        post.setTags("[]");
        post = postRepository.save(post);

        byte[] imageBytes = new byte[]{1, 2, 3, 4, 5};
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", imageBytes
        );

        mockMvc.perform(multipart("/api/posts/{id}/image", post.getId())
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk());

        Post updated = postRepository.findById(post.getId()).orElseThrow();
        assertArrayEquals(imageBytes, updated.getImage());
    }

    @Test
    void updateImage_shouldReturn404WhenPostNotFound() throws Exception {
        byte[] imageBytes = new byte[]{1};
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", imageBytes
        );

        mockMvc.perform(multipart("/api/posts/999/image")
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_shouldReturnImageBytes() throws Exception {
        Post post = new Post();
        post.setTitle("Image Post");
        post.setText("Text");
        post.setTags("[]");
        byte[] imageBytes = new byte[]{1, 2, 3};
        post.setImage(imageBytes);
        post = postRepository.save(post);

        mockMvc.perform(get("/api/posts/{id}/image", post.getId()))
                .andExpect(status().isOk())
                .andExpect(content().bytes(imageBytes));
    }

    @Test
    void getImage_shouldReturn404WhenPostNotFound() throws Exception {
        mockMvc.perform(get("/api/posts/999/image"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_shouldReturn404WhenImageIsNull() throws Exception {
        Post post = new Post();
        post.setTitle("No Image");
        post.setText("Text");
        post.setTags("[]");
        post.setImage(null);
        post = postRepository.save(post);

        mockMvc.perform(get("/api/posts/{id}/image", post.getId()))
                .andExpect(status().isNotFound());
    }
}