package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.ContentDir;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentDirDao extends JpaRepository<ContentDir, String> {
}
