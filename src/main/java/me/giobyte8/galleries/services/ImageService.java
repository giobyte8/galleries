package me.giobyte8.galleries.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.dto.Page;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.repositories.ImageRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final ImageRepository imgRepository;

    public Optional<Image> findByPath(String path) {
        return imgRepository.findByPath(path);
    }

    public Page<Image> getByParentDirId(
            UUID parentDirId,
            boolean recursive,
            Pageable pageable
    ) {
        if (recursive) {
            return Page.from(imgRepository.findByParentIdRecursively(
                    parentDirId,
                    pageable
            ));
        } else {
            return Page.from(imgRepository.findByParentId(
                    parentDirId,
                    pageable
            ));
        }
    }
}
