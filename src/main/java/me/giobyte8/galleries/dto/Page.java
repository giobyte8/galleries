package me.giobyte8.galleries.dto;

import java.util.List;

public record Page<T> (
         List<T> content,
         int pageIdx,
         int pageSize,
         int totalPages,
         long totalElements
) {
    public static <T> Page<T> from(
            org.springframework.data.domain.Page<T> springPage
    ) {
        return new Page<>(
                springPage.getContent(),
                springPage.getNumber(),
                springPage.getSize(),
                springPage.getTotalPages(),
                springPage.getTotalElements()
        );
    }
}
