package yandex.practicum.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import yandex.practicum.entity.Post;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Test
    void searchPosts_shouldReturnFilteredPosts() {
        Post post1 = new Post();
        post1.setTitle("Test Post 1");
        post1.setText("Test Text 1");
        post1.setTags("tag1");

        Post post2 = new Post();
        post2.setTitle("Test Post 2");
        post2.setText("Test Text 2");
        post2.setTags("tag2");

        postRepository.saveAll(List.of(post1, post2));

        Page<Post> page = postRepository.searchPosts("1", PageRequest.of(0, 10));

        assertEquals(1, page.getContent().size());
        assertTrue(page.getContent().getFirst().getTitle().contains("Test Post 1"));
    }
}