package me.giobyte8.galleries.scanner.dao;

import me.giobyte8.galleries.scanner.model.ContentDir;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentDirDao extends JpaRepository<ContentDir, String> {
}
