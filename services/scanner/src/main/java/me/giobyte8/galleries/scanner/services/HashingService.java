package me.giobyte8.galleries.scanner.services;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

@Service
public class HashingService {

    public String hashPath(String path) {
        return DigestUtils.sha256Hex(path);
    }
}
