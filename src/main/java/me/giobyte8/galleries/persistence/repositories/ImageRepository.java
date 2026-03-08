package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.persistence.models.ImageStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository
        extends CrudRepository<Image, String>, CustomizedImageRepository {

    long countByStatus(ImageStatus status);

    Image findByPath(String path);

    Image findByPathAndContentHash(String path, String contentHash);
}
