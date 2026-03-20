package me.giobyte8.galleries.api;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.dto.CreateDirectoryDto;
import me.giobyte8.galleries.dto.Page;
import me.giobyte8.galleries.exceptions.DirectoryNotFoundException;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.services.DirectoryService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/directories")
@RequiredArgsConstructor
public class DirectoriesController {

    private final DirectoryService directorySvc;

    /**
     * Get root directories,
     * i.e., directories that don't have a parent directory.
     *
     * @param pageable pagination information
     * @return a page of root directories sorted by path
     */
    @GetMapping
    public Page<Directory> getRoots(Pageable pageable) {
        return directorySvc.getRoots(pageable);
    }

    /**
     * Get directories contained in a given parent directory.
     * i.e., Direct children of a parent directory
     *
     * @param parentId id of the parent directory.
     * @return a list of directories contained in the given parent directory
     */
    @GetMapping("/{parentId}/directories")
    public Page<Directory> getChildren(
            @PathVariable UUID parentId,
            Pageable pageable
    ) {
        return directorySvc.getChildren(parentId, pageable);
    }

    @GetMapping("/{directoryId}")
    public Directory getById(@PathVariable UUID directoryId) {
        return directorySvc
                .getById(directoryId)
                .orElseThrow(() -> new DirectoryNotFoundException(
                        "Directory with id '%s' not found".formatted(directoryId)
                ));
    }

    @PostMapping
    public Directory createDirectory(
            @RequestBody CreateDirectoryDto createDirDto
    ) {
        // TODO Validate path
        return directorySvc.createDirectory(createDirDto);
    }

    @ExceptionHandler(DirectoryNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleDirectoryNotFound(DirectoryNotFoundException ex) {
        return ex.getMessage();
    }
}
