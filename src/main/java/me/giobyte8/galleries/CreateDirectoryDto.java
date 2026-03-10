package me.giobyte8.galleries;

public record CreateDirectoryDto(
        String path,
        boolean recursive) {
}
