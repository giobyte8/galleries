package me.giobyte8.galleries.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.persistence.models.Directory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DirectoryService {

    /**
     * Get directories contained in a given parent ID.
     * If parentId is null, return root directories.
     *
     * @param parentId the parent directory ID, or null for root directories
     * @return a list of directories contained in the given parent directory
     */
    public List<Directory> directories(UUID parentId) {
        return Collections.emptyList();
    }
}
