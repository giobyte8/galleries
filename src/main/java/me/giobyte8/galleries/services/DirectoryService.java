package me.giobyte8.galleries.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final DirectoryRepository dirRepository;

    /**
     * Get directories contained in a given parent directory.
     * If parentPath is null, return root directories.
     *
     * @param parentPath the parent directory, or null for root directories
     * @return a list of directories contained in the given parent directory
     */
    public List<Directory> directories(String parentPath) {
        return Objects.isNull(parentPath)
                ? dirRepository.findRoots()
                : dirRepository.findChildren(parentPath);
    }
}
