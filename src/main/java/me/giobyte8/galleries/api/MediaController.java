package me.giobyte8.galleries.api;

import lombok.RequiredArgsConstructor;
import me.giobyte8.galleries.dto.MediaItemDto;
import me.giobyte8.galleries.dto.Page;
import me.giobyte8.galleries.services.MediaService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MediaController {
    private final MediaService mediaSvc;

    @GetMapping("/directories/{directoryId}/media")
    public Page<MediaItemDto> getByParent(
            @PathVariable UUID directoryId,
            @RequestParam(defaultValue = "false") boolean recursive,
            Pageable pageable
    ) {
        return mediaSvc.getByParentId(directoryId, recursive, pageable);
    }
}
