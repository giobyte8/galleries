package me.giobyte8.galleries.api;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.CreateDirectoryDto;
import me.giobyte8.galleries.exceptions.DirectoryNotFoundException;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.services.DirectoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/directories")
@RequiredArgsConstructor
public class DirectoriesController {

    private final DirectoryService directorySvc;

    @PostMapping
    public Directory createDirectory(
            @RequestBody CreateDirectoryDto createDirDto
    ) {
        // TODO Validate path
        return directorySvc.createDirectory(createDirDto);
    }

    @GetMapping
    public Directory directoryByPath(@RequestParam String path) {
        return directorySvc
                .directoryByPath(path)
                .orElseThrow(() -> new DirectoryNotFoundException(path));
    }

    // Handle DirectoryNotFoundException and return 404
    @ExceptionHandler(DirectoryNotFoundException.class)
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    public String handleDirectoryNotFound(DirectoryNotFoundException ex) {
        return ex.getMessage();
    }
}
