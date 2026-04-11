package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.dto.MediaItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MediaRepository {

    /**
     * Returns a paginated mix of images and videos that are direct
     * children of the given directory.
     * <p>
     * Sorts by the order in {@code pageable}; defaults to
     * {@code captureInstant DESC, path ASC} when unsorted.
     */
    Page<MediaItemDto> findAllMediaByParentId(
            UUID parentId,
            Pageable pageable
    );

    /**
     * Returns a paginated mix of images and videos that are descendants
     * (any depth) of the given directory.
     * <p>
     * Sorts by the order in {@code pageable}; defaults to
     * {@code captureInstant DESC, path ASC} when unsorted.
     */
    Page<MediaItemDto> findAllMediaByParentIdRecursively(
            UUID parentId,
            Pageable pageable
    );
}
