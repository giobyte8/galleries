package me.giobyte8.galleries.persistence.mappers;

import me.giobyte8.galleries.persistence.models.DirStatus;
import me.giobyte8.galleries.persistence.models.Directory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DirRowMapper {

    public Directory from(Map<String, Object> dirMap) {
        Number versionNumber = (Number) dirMap.get("version");
        Long version = versionNumber == null ? null : versionNumber.longValue();

        return Directory
                .builder()
                .path((String) dirMap.get("path"))
                .version(version)
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
