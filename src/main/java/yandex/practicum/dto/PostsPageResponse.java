package yandex.practicum.dto;

import java.util.List;

public record PostsPageResponse(
        List<PostResponse> posts,
        boolean hasPrev,
        boolean hasNext,
        int lastPage
) {}