package me.giobyte8.galleries.api;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.exceptions.DirectoryNotFoundException;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.services.ImageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class ImagesController {
    private final ImageService imageSvc;

    @GetMapping
    public List<Image> findByParent(
            @RequestParam String parentPath
    ) throws DirectoryNotFoundException {
        return imageSvc
                .findByParent(parentPath)
                .toList();
    }
}
