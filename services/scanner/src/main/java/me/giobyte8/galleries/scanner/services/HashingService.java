package me.giobyte8.galleries.scanner.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class HashingService {

    public String hashPath(String path) {
        return DigestUtils.sha256Hex(path);
    }

    public String hashPath(Path path) {
        return this.hashPath(path.toString());
    }

    public String hashFile(Path fPath) throws IOException {
        InputStream is = Files.newInputStream(fPath);
        return DigestUtils.sha256Hex(is);
    }
}
