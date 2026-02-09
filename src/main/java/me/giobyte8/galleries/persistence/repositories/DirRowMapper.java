package me.giobyte8.galleries.persistence.repositories;

import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.models.Directory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DirRowMapper {

    public Directory from(Map<String, Object> dirMap) {
        return Directory
                .builder()
                .path((String) dirMap.get("path"))
                .recursive((Boolean) dirMap.get("recursive"))
                .status(DirStatus.valueOf((String) dirMap.get("status")))
                .build();
    }

    public Map<String, Object> asMap(Directory dir) {
        Map<String, Object> dirMap = new HashMap<>(3);
        dirMap.put("path", dir.getPath());
        dirMap.put("recursive", dir.isRecursive());
        dirMap.put("status", dir.getStatus().toString());

        return dirMap;
    }
}
