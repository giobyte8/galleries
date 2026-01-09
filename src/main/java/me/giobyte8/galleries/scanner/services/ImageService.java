package me.giobyte8.galleries.scanner.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.scanner.model.Image;
import me.giobyte8.galleries.scanner.repository.ImageRepository;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ImageService {
    private final ImageRepository imageRepo;

    public Image findByPath(String path) {
        return imageRepo.findByPath(path);
    }
}
