package me.giobyte8.galleries.scanner.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.repositories.ImageRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final ImageRepository imageRepo;

    public Image findByPath(String path) {
        return imageRepo.findByPath(path);
    }
}
