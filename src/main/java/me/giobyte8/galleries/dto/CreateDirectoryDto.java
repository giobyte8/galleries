package me.giobyte8.galleries.dto;

public record CreateDirectoryDto(
        String path,
        boolean recursive) {
}
