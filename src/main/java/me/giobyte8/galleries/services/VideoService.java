package me.giobyte8.galleries.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.persistence.models.Video;
import me.giobyte8.galleries.persistence.repositories.VideoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class VideoService {
    private final VideoRepository videoRepository;

    public Optional<Video> findByPath(String path) {
        return videoRepository.findByPath(path);
    }
}

