package me.giobyte8.galleries.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.exceptions.DirectoryNotFoundException;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import me.giobyte8.galleries.persistence.repositories.ImageRepository;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final DirectoryRepository dirRepository;
    private final ImageRepository imgRepository;

    public Image findByPath(String path) {
        return imgRepository.findByPath(path);
    }

    public Stream<Image> findByParent(String parentPath)
            throws DirectoryNotFoundException {

        var parent = dirRepository.findBy(parentPath);
        if (parent == null) throw new DirectoryNotFoundException(parentPath);

        return imgRepository.findBy(parent);
    }
}
