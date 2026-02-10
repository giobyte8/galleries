package me.giobyte8.galleries.api;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.persistence.models.Directory;
import me.giobyte8.galleries.services.DirectoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/directories")
@RequiredArgsConstructor
public class DirectoriesController {

    private final DirectoryService directorySvc;

    @GetMapping
    public List<Directory> directories(
            @RequestParam(required = false) String parentPath
    ) {
        return directorySvc.directories(parentPath);
    }
}
