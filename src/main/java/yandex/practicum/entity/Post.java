package yandex.practicum.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import yandex.practicum.dto.PostRequest;

import java.util.List;

@Entity
@Table(name = "posts")
@NoArgsConstructor
@Data
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(nullable = false)
    private String text;

    @Column
    private String tags;

    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    @Column(columnDefinition = "bytea")
    private byte[] image;

    public Post(PostRequest request) {
        this.title = request.title();
        this.text = request.text();
        setTagsFromList(request.tags());
    }

    public List<String> getTagsAsList() {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        try {
            return new ObjectMapper().readValue(tags, new TypeReference<>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка десериализации тегов", e);
        }
    }

    public void setTagsFromList(List<String> tagList) {
        try {
            this.tags = new ObjectMapper().writeValueAsString(tagList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации тегов", e);
        }
    }
}