package me.giobyte8.galleries.api;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.dto.Page;
import me.giobyte8.galleries.persistence.models.Image;
import me.giobyte8.galleries.services.ImageService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImagesController {
    private final ImageService imageSvc;

    @GetMapping("/directories/{directoryId}/images")
    public Page<Image> getByDirectory(
            @PathVariable UUID directoryId,
            @RequestParam(defaultValue = "false") boolean recursive,
            Pageable pageable
    ) {
        return imageSvc.getByParentDirId(directoryId, recursive, pageable);
    }
}
