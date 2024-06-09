package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;

import java.util.Set;

public interface DirectoryRepository {

    int count();

    Directory findBy(String path);

    void save(Directory directory);

    void save(Directory directory, Set<Image> images);

    void addImage(String dirPath, Image image);

    void addImages(String dirPath, Set<Image> images);
}
