package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.MediaFileStatus;
import me.giobyte8.galleries.persistence.models.Video;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VideoRepository extends
        CrudRepository<Video, String>,
        CustomizedVideoRepository {

    long countByStatus(MediaFileStatus status);

    Optional<Video> findByPath(String path);
}

