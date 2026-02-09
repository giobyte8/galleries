package me.giobyte8.galleries.api;

import me.giobyte8.galleries.persistence.models.Directory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class GalleriesController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }

    @GetMapping("/directories")
    public List<Directory> directories(
            @RequestParam(required = false) UUID parent
    ) {
        return Collections.emptyList();
    }
}
