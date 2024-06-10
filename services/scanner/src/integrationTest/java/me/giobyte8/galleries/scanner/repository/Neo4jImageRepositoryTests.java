package me.giobyte8.galleries.scanner.repository;

import me.giobyte8.galleries.scanner.model.Directory;
import me.giobyte8.galleries.scanner.model.Image;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
public class Neo4jImageRepositoryTests {

    @Autowired
    private Neo4jImageRepository imgRepository;

    @Autowired
    private Neo4jDirectoryRepository dirRepository;

    @Test
    void save() {
        String dirPath = "test/dir/img/repo";
        Directory parent = Directory.builder()
                .path(dirPath)
                .build();
        dirRepository.save(parent);

        Image img = Image.builder()
                .path("random.jpg")
                .contentHash("12345")
                .datetimeOriginal(LocalDateTime.MIN)
                .gpsLatitude(BigDecimal.ONE)
                .gpsLongitude(BigDecimal.TEN)
                .cameraMaker("Samsung")
                .cameraModel("S23 Ultra")
                .build();

        imgRepository.save(parent, img);

        // Assert image was saved
        Image dbImg = imgRepository.findBy(img.getPath());
        assert dbImg != null;

        // Assert image was added to parent dir
    }

    @Test
    void findNonExistent() {
        String path = "test/not/found";
        Image img = imgRepository.findBy(path);
        assertNull(img);
    }
}
