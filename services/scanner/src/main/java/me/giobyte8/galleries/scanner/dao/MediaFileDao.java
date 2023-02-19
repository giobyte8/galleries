package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MediaFileDao extends JpaRepository<MediaFile, String> {
}
