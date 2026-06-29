package me.giobyte8.galleries.services;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.dto.CreateDirectoryDto;
import me.giobyte8.galleries.dto.Page;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.persistence.projections.DirWithLineage;
import me.giobyte8.galleries.persistence.repositories.DirectoryRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DirectoryService {

    private final DirectoryRepository dirRepository;

    public Page<Directory> getRoots(Pageable pageable) {
        return Page.from(dirRepository.findRoots(pageable));
    }

    public Page<Directory> getChildren(UUID parentId, Pageable pageable) {
        return Page.from(dirRepository.findChildren(parentId, pageable));
    }

    public Optional<Directory> getById(UUID directoryId) {
        return dirRepository.findById(directoryId);
    }

    public Optional<DirWithLineage> getWithLineageById(UUID directoryId) {
        return dirRepository.findWithLineageById(directoryId);
    }

    public Directory createDirectory(CreateDirectoryDto createDirDto) {
        var dir = Directory.builder()
                .path(createDirDto.path())
                .recursive(createDirDto.recursive())
                .build();

        // TODO Handle non unique 'path' error
        return dirRepository.save(dir);
    }

    public List<Directory> searchByPath(String query) {
        if (!StringUtils.hasText(query)) {
            return List.of();
        }

        return dirRepository.searchByPath(query.trim(), 5);
    }
}
