package yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import yandex.practicum.dto.PostRequest;
import yandex.practicum.dto.PostResponse;
import yandex.practicum.dto.PostsPageResponse;
import yandex.practicum.service.PostService;

import java.io.IOException;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public PostsPageResponse getPosts(@RequestParam(name = "search", required = false, defaultValue = "") String search,
                                      @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
                                      @RequestParam(name = "pageSize", defaultValue = "5") int pageSize) {

        return postService.getAllPosts(search, pageNumber, pageSize);
    }

    @GetMapping("/{id}")
    public PostResponse getPostById(@PathVariable("id") Long id) {
        return postService.getPostById(id);

    }

    @PostMapping
    public PostResponse createPost(@RequestBody PostRequest request) {
        return postService.createPost(request);
    }

    @PutMapping("/{id}")
    public PostResponse updatePost(@PathVariable("id") Long id, @RequestBody PostRequest request) {
        return postService.updatePost(id, request);
    }

    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable("id") Long id) {
        postService.deletePost(id);
    }

    @PostMapping("/{id}/likes")
    public Integer incrementLikes(@PathVariable("id") Long id) {
        return postService.incrementLikes(id);
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void updateImage(@PathVariable("id") Long id, @RequestParam("image") MultipartFile image) throws IOException {
        postService.updateImage(id, image);
    }

    @GetMapping("/{id}/image")
    public byte[] getImage(@PathVariable("id") Long id) {
        return postService.getImage(id);
    }
}