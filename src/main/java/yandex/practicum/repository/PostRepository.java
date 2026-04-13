package yandex.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yandex.practicum.entity.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT * FROM posts p WHERE (:search = '' OR p.title ILIKE CONCAT('%', :search, '%') OR p.text ILIKE CONCAT('%', :search, '%'))",
            countQuery = "SELECT COUNT(*) FROM posts p WHERE (:search = '' OR p.title ILIKE CONCAT('%', :search, '%') OR p.text ILIKE CONCAT('%', :search, '%'))",
            nativeQuery = true)
    Page<Post> searchPosts(@Param("search") String search, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Post p SET p.likesCount = p.likesCount + 1 WHERE p.id = :postId")
    int incrementLikes(@Param("postId") Long postId);
}