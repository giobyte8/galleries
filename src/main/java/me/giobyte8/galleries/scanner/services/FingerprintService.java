package me.giobyte8.galleries.scanner.services;

import me.giobyte8.galleries.scanner.dto.Fingerprint;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FingerprintService {

    public Fingerprint forPath(Path absPath) throws IOException {
        return new Fingerprint(
                Files.size(absPath),
                Files.getLastModifiedTime(absPath).toInstant()
        );
    }
}

