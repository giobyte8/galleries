package me.giobyte8.galleries.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.dto.MediaItemDto;
import me.giobyte8.galleries.dto.Page;
import me.giobyte8.galleries.persistence.repositories.MediaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MediaService {
    private final MediaRepository mediaRepository;

    public Page<MediaItemDto> getByParentId(
            UUID parentDirId,
            boolean recursive,
            Pageable pageable
    ) {
        if (recursive) {
            return Page.from(mediaRepository.findAllMediaByParentIdRecursively(
                    parentDirId,
                    pageable
            ));
        }

        return Page.from(mediaRepository.findAllMediaByParentId(
                parentDirId,
                pageable
        ));
    }
}
