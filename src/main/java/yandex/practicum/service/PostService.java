package yandex.practicum.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import yandex.practicum.dto.PostRequest;
import yandex.practicum.dto.PostResponse;
import yandex.practicum.dto.PostsPageResponse;
import yandex.practicum.entity.Post;
import yandex.practicum.exception.ImageNotFoundException;
import yandex.practicum.exception.PostNotFoundException;
import yandex.practicum.repository.PostRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public PostsPageResponse getAllPosts(String search, int pageNumber, int pageSize) {
        int pageIndex = Math.max(0, pageNumber - 1);
        Pageable pageable = PageRequest.of(pageIndex, pageSize);

        Page<Post> page;
        if (search == null || search.isBlank()) {
            page = postRepository.findAll(pageable);
        } else {
            page = postRepository.searchPosts(search, pageable);
        }

        List<PostResponse> posts = page.getContent().stream()
                .map(PostResponse::from)
                .collect(Collectors.toList());
        return new PostsPageResponse(posts, pageNumber > 1, page.hasNext(), page.getTotalPages());
    }

    @Transactional(readOnly = true)
    public PostResponse getPostById(Long id) {
        return postRepository.findById(id).map(PostResponse::from).orElseThrow(() -> new PostNotFoundException(id));
    }

    @Transactional
    public PostResponse createPost(PostRequest request) {
        Post saved = postRepository.save(new Post(request));
        return PostResponse.from(saved);
    }

    @Transactional
    public PostResponse updatePost(Long id, PostRequest request) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        post.setTitle(request.title());
        post.setText(request.text());
        post.setTagsFromList(request.tags());
        Post updated = postRepository.save(post);
        return PostResponse.from(updated);
    }

    @Transactional
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Transactional
    public int incrementLikes(Long id) {
        int updated = postRepository.incrementLikes(id);
        if (updated == 0) {
            throw new PostNotFoundException(id);
        }

        return postRepository.findById(id).map(Post::getLikesCount)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    @Transactional
    public void updateImage(Long id, MultipartFile file) throws IOException {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        post.setImage(file.getBytes());
        postRepository.save(post);
    }

    @Transactional
    public byte[] getImage(Long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        if (post.getImage() == null) {
            throw new ImageNotFoundException(id);
        }
        return post.getImage();
    }
}