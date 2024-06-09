package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.ImageStatus;
import me.giobyte8.galleries.scanner.model.Image;

import java.util.stream.Stream;

public interface ImageRepository {

    long countBy(ImageStatus status);

    Stream<Image> findBy(String dirPath);

    Stream<Image> findBy(String dirPath, ImageStatus status);

    void save(Image image);

}
