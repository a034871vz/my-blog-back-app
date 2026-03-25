package yandex.practicum.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import yandex.practicum.dto.PostResponse;
import yandex.practicum.dto.PostRequest;
import yandex.practicum.entity.Post;
import yandex.practicum.repository.PostRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @InjectMocks
    private PostService postService;

    @Test
    void getPostsPage_shouldReturnPageResponse() {
        String search = "test";
        int pageNumber = 1;
        int pageSize = 5;
        Pageable pageable = PageRequest.of(0, pageSize);

        Post post = new Post();
        post.setId(1L);
        post.setTitle("Test Post");
        post.setText("Test text");
        post.setTags("[\"tag\"]");
        post.setLikesCount(0);
        post.setCommentsCount(0);

        Page<Post> page = new PageImpl<>(List.of(post), pageable, 1);
        Mockito.when(postRepository.searchPosts(ArgumentMatchers.eq(search), ArgumentMatchers.eq(pageable))).thenReturn(page);

        var response = postService.getAllPosts(search, pageNumber, pageSize);

        Assertions.assertNotNull(response);
        assertEquals(1, response.posts().size());
        assertFalse(response.hasPrev());
        assertFalse(response.hasNext());
        assertEquals(1, response.lastPage());

        PostResponse postResponse = response.posts().getFirst();
        assertEquals(1L, postResponse.id());
        assertEquals("Test Post", postResponse.title());
        assertEquals("Test text", postResponse.text());
        assertEquals(List.of("tag"), postResponse.tags());
        assertEquals(0, postResponse.likesCount());
        assertEquals(0, postResponse.commentsCount());

        Mockito.verify(postRepository).searchPosts(ArgumentMatchers.eq(search), ArgumentMatchers.eq(pageable));
    }

    @Test
    void getPostById_shouldReturnPostWhenExists() {
        Long id = 1L;
        Post post = new Post();
        post.setId(id);
        post.setTitle("Test");
        Mockito.when(postRepository.findById(id)).thenReturn(Optional.of(post));

        PostResponse result = postService.getPostById(id);
        assertEquals(id, result.id());
    }

    @Test
    void getPostById_shouldReturnEmptyWhenNotExists() {
        Long id = 999L;
        Mockito.when(postRepository.findById(id)).thenReturn(Optional.empty());

        PostResponse result = postService.getPostById(id);
        Assertions.assertNull(result);
    }

    @Test
    void createPost_shouldSaveAndReturnResponse() {
        PostRequest request = new PostRequest("Title", "Text", List.of("tag1", "tag2"));
        Post savedPost = new Post(request);
        savedPost.setId(1L);
        Mockito.when(postRepository.save(ArgumentMatchers.any(Post.class))).thenReturn(savedPost);

        PostResponse response = postService.createPost(request);

        Assertions.assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Title", response.title());
        assertEquals("Text", response.text());
        assertEquals(List.of("tag1", "tag2"), response.tags());
        assertEquals(0, response.likesCount());
        assertEquals(0, response.commentsCount());
        Mockito.verify(postRepository).save(ArgumentMatchers.any(Post.class));
    }

    @Test
    void updatePost_shouldUpdateAndReturnResponseWhenExists() {
        Long id = 1L;
        Post existing = new Post();
        existing.setId(id);
        existing.setTitle("Old");
        existing.setText("Old text");
        existing.setTags("[\"old\"]");

        PostRequest request = new PostRequest("New Title", "New Text", List.of("new"));
        Post updated = new Post(request);
        updated.setId(id);

        Mockito.when(postRepository.findById(id)).thenReturn(Optional.of(existing));
        Mockito.when(postRepository.save(ArgumentMatchers.any(Post.class))).thenReturn(updated);

        PostResponse response = postService.updatePost(id, request);

        assertEquals(id, response.id());
        assertEquals("New Title", response.title());
        assertEquals("New Text", response.text());
        assertEquals(List.of("new"), response.tags());
        assertEquals("New Title", existing.getTitle());
        assertEquals("New Text", existing.getText());
        Assertions.assertNotNull(existing.getTags());
        Mockito.verify(postRepository).save(existing);
    }

    @Test
    void updatePost_shouldThrowWhenNotFound() {
        Long id = 999L;
        PostRequest request = new PostRequest("Title", "Text", List.of());
        Mockito.when(postRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(RuntimeException.class, () -> postService.updatePost(id, request));
        Mockito.verify(postRepository, Mockito.never()).save(ArgumentMatchers.any());
    }

    @Test
    void deletePost_shouldCallRepositoryDeleteById() {
        Long id = 1L;

        postService.deletePost(id);

        Mockito.verify(postRepository).deleteById(id);
    }

    @Test
    void incrementLikes_shouldReturnNewLikesCount() {
        Long id = 1L;
        Post post = new Post();
        post.setId(id);
        post.setLikesCount(5);
        Mockito.when(postRepository.incrementLikes(id)).thenReturn(1);
        Mockito.when(postRepository.findById(id)).thenReturn(Optional.of(post));

        int newLikes = postService.incrementLikes(id);
        assertEquals(5, newLikes);
        Mockito.verify(postRepository).incrementLikes(id);
        Mockito.verify(postRepository).findById(id);
    }

    @Test
    void incrementLikes_shouldThrowWhenPostNotFound() {
        Long id = 999L;
        Mockito.when(postRepository.incrementLikes(id)).thenReturn(0);

        Assertions.assertThrows(RuntimeException.class, () -> postService.incrementLikes(id));
        Mockito.verify(postRepository).incrementLikes(id);
        Mockito.verify(postRepository, Mockito.never()).findById(ArgumentMatchers.any());
    }

    @Test
    void updateImage_shouldSaveImageBytes() throws Exception {
        Long id = 1L;
        byte[] imageBytes = new byte[]{1, 2, 3};
        MultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpeg", imageBytes);
        Post post = new Post();
        post.setId(id);
        Mockito.when(postRepository.findById(id)).thenReturn(Optional.of(post));

        postService.updateImage(id, file);

        Assertions.assertArrayEquals(imageBytes, post.getImage());
        Mockito.verify(postRepository).save(post);
    }

    @Test
    void updateImage_shouldThrowWhenPostNotFound() {
        Long id = 999L;
        byte[] imageBytes = new byte[0];
        MultipartFile file = new MockMultipartFile("image", "image.jpg", "image/jpeg", imageBytes);
        Mockito.when(postRepository.findById(id)).thenReturn(Optional.empty());

        Assertions.assertThrows(RuntimeException.class, () -> postService.updateImage(id, file));
        Mockito.verify(postRepository, Mockito.never()).save(ArgumentMatchers.any());
    }

    @Test
    void getImage_shouldReturnImageBytesWhenExists() {
        Long id = 1L;
        byte[] imageBytes = new byte[]{1, 2, 3};
        Post post = new Post();
        post.setId(id);
        post.setImage(imageBytes);
        Mockito.when(postRepository.findById(id)).thenReturn(Optional.of(post));

        byte[] result = postService.getImage(id);
        Assertions.assertArrayEquals(imageBytes, result);
    }

    @Test
    void getImage_shouldThrowWhenPostNotFound() {
        Long id = 999L;
        Mockito.when(postRepository.findById(id)).thenReturn(Optional.empty());
        Assertions.assertThrows(RuntimeException.class, () -> postService.getImage(id));
    }

    @Test
    void getImage_shouldThrowWhenImageIsNull() {
        Long id = 1L;
        Post post = new Post();
        post.setId(id);
        post.setImage(null);
        Mockito.when(postRepository.findById(id)).thenReturn(Optional.of(post));

        Assertions.assertThrows(RuntimeException.class, () -> postService.getImage(id));
    }
}